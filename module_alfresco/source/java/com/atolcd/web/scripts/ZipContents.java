/*
 * Copyright (C) 2012 Atol Conseils et D�veloppements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.atolcd.web.scripts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.ibm.icu.text.Normalizer;

public class ZipContents extends AbstractWebScript {
	private static Log logger = LogFactory.getLog(ZipContents.class);

	private static final int BUFFER_SIZE = 1024;

	private static final String MIMETYPE_ZIP = "application/zip";
	private static final String TEMP_FILE_PREFIX = "alf";
	private static final String ZIP_EXTENSION = ".zip";

	private ContentService contentService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private StoreRef storeRef;
	private String encoding;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setStoreUrl(String url) {
		this.storeRef = new StoreRef(url);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodes = req.getParameter("nodes");
		if (nodes == null || nodes.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "nodes");
		}

		List<String> nodeIds = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(nodes, ",");
		if (tokenizer.hasMoreTokens()) {
			while (tokenizer.hasMoreTokens()) {
				nodeIds.add(tokenizer.nextToken());
			}
		}

		String filename = req.getParameter("filename");
		if (filename == null || filename.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "filename");
		}

		String noaccentStr = req.getParameter("noaccent");
		if (noaccentStr == null || noaccentStr.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "noaccent");
		}

		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment;filename=\"" + unAccent(filename) + ZIP_EXTENSION + "\"");

			res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");

			createZipFile(nodeIds, res.getOutputStream(), new Boolean(noaccentStr));
		} catch (RuntimeException e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Erreur lors de la g�n�ration de l'archive.");
		}
	}

	public void createZipFile(List<String> nodeIds, OutputStream os, boolean noaccent) throws IOException {
		File zip = null;

		try {
			if (nodeIds != null && !nodeIds.isEmpty()) {
				zip = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, ZIP_EXTENSION);
				FileOutputStream stream = new FileOutputStream(zip);
				CheckedOutputStream checksum = new CheckedOutputStream(stream, new Adler32());
				BufferedOutputStream buff = new BufferedOutputStream(checksum);
				ZipArchiveOutputStream out = new ZipArchiveOutputStream(buff);
				out.setEncoding(encoding);
				out.setMethod(ZipArchiveOutputStream.DEFLATED);
				out.setLevel(Deflater.BEST_COMPRESSION);
				
        if (logger.isDebugEnabled()) {
          logger.debug("Using encoding '" + encoding + "' for zip file.");
        }

				try {
					for (String nodeId : nodeIds) {
						NodeRef node = new NodeRef(storeRef, nodeId);
						addToZip(node, out, noaccent, "");
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new WebScriptException(
							HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				} finally {
					out.close();
					buff.close();
					checksum.close();
					stream.close();

					if (nodeIds.size() > 0) {
						InputStream in = new FileInputStream(zip);
						try {
						  byte[] buffer = new byte[BUFFER_SIZE];
						  int len;

						  while ((len = in.read(buffer)) > 0) {
						    os.write(buffer, 0, len);
						  }
						} finally {
						  IOUtils.closeQuietly(in);
						}
					}
				}
			}
		} catch (Exception e) {
		  logger.error(e.getMessage(), e);
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
		finally {
			// try and delete the temporary file
			if (zip != null) {
				zip.delete();
			}
		}
	}

	public void addToZip(NodeRef node, ZipArchiveOutputStream out, boolean noaccent, String path) throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);

		// Special case : links
		if (this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FILELINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(node, ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode == null) {
				return;
			}

			// Duplicate entry: check if link is not in the same space of the link destination
			if (nodeService.getPrimaryParent(node).getParentRef().equals(nodeService.getPrimaryParent(linkDestinationNode).getParentRef())) {
				return;
			}

			nodeQnameType = this.nodeService.getType(linkDestinationNode);
			node = linkDestinationNode;
		}

		String nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
		nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();

				String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

				ZipArchiveEntry entry = new ZipArchiveEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED)).getTime());

				entry.setSize(reader.getSize());
				out.putArchiveEntry(entry);

				byte buffer[] = new byte[BUFFER_SIZE];
				while (true) {
					int nRead = is.read(buffer, 0, buffer.length);
					if (nRead <= 0) {
						break;
					}

					out.write(buffer, 0, nRead);
				}
				is.close();
				out.closeArchiveEntry();
			}
			else {
				logger.warn("Could not read : "	+ nodeName + "content");
			}
		}
		else if(this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER) 
				&& !this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_SYSTEM_FOLDER)) {
			List<ChildAssociationRef> children = nodeService
					.getChildAssocs(node);
			if (children.isEmpty()) {
				String folderPath = path.isEmpty() ? nodeName + '/' : path + '/' + nodeName + '/';
				out.putArchiveEntry(new ZipArchiveEntry(new ZipEntry(folderPath)));
			} else {
				for (ChildAssociationRef childAssoc : children) {
					NodeRef childNodeRef = childAssoc.getChildRef();

					addToZip(childNodeRef, out, noaccent,
							path.isEmpty() ? nodeName : path + '/' + nodeName);
				}
			}
		} else {
			logger.info("Unmanaged type: "
					+ nodeQnameType.getPrefixedQName(this.namespaceService)
					+ ", filename: " + nodeName);
		}
	}

	/**
	 * ZipEntry() does not convert filenames from Unicode to platform (waiting
	 * Java 7) http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499
	 * 
	 * @param s
	 * @return
	 */
	public static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.NFD, 0);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}

}
