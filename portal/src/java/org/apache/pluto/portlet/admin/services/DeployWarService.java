/*
 * Copyright 2003,2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.portlet.admin.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.PortletDiskFileUpload;
import org.apache.pluto.portlet.admin.BaseAdminObject;
import org.apache.pluto.portlet.admin.PlutoAdminConstants;
import org.apache.pluto.portlet.admin.PlutoAdminException;
import org.apache.pluto.portlet.admin.bean.PageTO;
import org.apache.pluto.portlet.admin.bean.PortletMessage;
import org.apache.pluto.portlet.admin.bean.PortletMessageType;
import org.apache.pluto.portlet.admin.bean.PortletTO;
import org.apache.pluto.portlet.admin.model.PageRegistryXao;
import org.apache.pluto.portlet.admin.model.PortletEntityRegistryXao;
import org.apache.pluto.portlet.admin.util.PlutoAdminContext;

/**
 * DeployWarService
 *
 * @author Ken Atherton
 * @author Craig Doremus
 *
 */
public class DeployWarService extends BaseAdminObject {

  public static final String ERROR_NO_FILE = "ERROR_NO_FILE";
	public static final String CLASS_NAME = "DeployWarService";

	/**
	 *
	 */
	public DeployWarService() {
		super(CLASS_NAME);
	}

	/**
	 * @param className
	 * @param logId
	 */
	public DeployWarService(String logId) {
		super(CLASS_NAME, logId);
	}

  public String processFileUpload(ActionRequest request, ActionResponse response) {
  	final String METHOD_NAME = "processFileUpload(request,response)";
    String fileName = null;
    String serverFileName = null;
    request.getPortletSession().setAttribute(PlutoAdminConstants.MESSAGE_ATTR, new PortletMessage("Deployment unsuccessful", PortletMessageType.ERROR));
    // Check the request content type to see if it starts with multipart/
    if (PortletDiskFileUpload.isMultipartContent(request))
    {

	    PortletDiskFileUpload dfu = new PortletDiskFileUpload();

	    //maximum allowed file upload size (10 MB)
	    dfu.setSizeMax(10 * 1000 * 1000);

	    //maximum size in memory (vs disk) (100 KB)
	    dfu.setSizeThreshold(100 * 1000);

        try
        {
            //get the FileItems
            List fileItems = dfu.parseRequest(request);
            Iterator iter = fileItems.iterator();
            while (iter.hasNext())
            {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField())
                {
                    //pass along to render request
                    String fieldName = item.getFieldName();
                    String value = item.getString();
                    response.setRenderParameter(fieldName, value);
                }
                else
                {
                    //write the uploaded file to a new location
                    fileName = item.getName();
                    String contentType = item.getContentType();
                    long size = item.getSize();
                    response.setRenderParameter("size", Long.toString(size));
                    response.setRenderParameter("contentType", contentType);
                    String tempDir = System.getProperty("java.io.tmpdir");
                    serverFileName = getRootFilename(File.separatorChar, fileName);
                    File serverFile = new File(tempDir, serverFileName);
                    item.write(serverFile);
                    response.setRenderParameter("serverFileName",  serverFileName);
                    logDebug(METHOD_NAME, "serverFileName : " + tempDir + PlutoAdminConstants.FS + serverFileName);

                    //Add to portletentityregistry.xml
										int index = serverFileName.indexOf(".war");
										String context = "";
										if ( index != -1) {
											context = serverFileName.substring(0, index);
										} else {
											context = serverFileName;
										}
										//TODO: send in boolean for existance of PER
					          //Check to see if a record exists
					          PortletEntityRegistryXao xao = new PortletEntityRegistryXao();
					          boolean appExists = xao.applicationExists(context);
										ArrayList  argList = createDeploymentArgs(serverFileName, tempDir, request, appExists);
										Map pmap = (HashMap) request.getPortletSession().getAttribute(PlutoAdminConstants.PORTLET_MAP_ATTR);
										logDebug(METHOD_NAME, "pmap: " + mapToEntrySetString(pmap));
										String[] args = arrayListToStringArray(argList);
										for (int i =0; i < args.length; i++) {
											logDebug(METHOD_NAME, "args["+i+"]="+args[i]);
										}
				            org.apache.pluto.portalImpl.Deploy.main(args);
				            if (appExists) {
				            	request.getPortletSession().setAttribute(PlutoAdminConstants.MESSAGE_ATTR, new PortletMessage("Deployment of the new portlet app has been successful, but the portlet app record " + context + " already exists in portletentityregistry.xml. If you are deploying a previously deployed portlet app, caching of the old app may require that you restart Pluto to see the new changes.", PortletMessageType.INFO));
				            } else {
				            	request.getPortletSession().setAttribute(PlutoAdminConstants.MESSAGE_ATTR, new PortletMessage("Deployment and addition to portletentityregistry.xml successful", PortletMessageType.SUCCESS));
				            }
				         }
            }
        }
        catch (FileUploadException e){
            String msg = "File Upload Exception: " + e.getMessage();
            logError(METHOD_NAME, msg, e);
            throw new PlutoAdminException(e);
        } catch (Exception e) {
            String msg = "Exception: " + e.getMessage();
            logError(METHOD_NAME, msg, e);
            throw new PlutoAdminException(e);
        }
    } else {
        //set an error message
      	request.getPortletSession().setAttribute(PlutoAdminConstants.MESSAGE_ATTR, new PortletMessage("No file appears to have been selected.", PortletMessageType.ERROR));
    }
    logMethodEnd(METHOD_NAME, serverFileName);
    return serverFileName;
  }


  private String getRootFilename(char delimiter, String pathName) {
    int startFilenameIndex = pathName.lastIndexOf(delimiter)  + 1;
    String filename =  pathName.substring(startFilenameIndex);
    return filename;
  }

	private static String[] arrayListToStringArray(ArrayList argStringArrayList) {
		return  (String[]) argStringArrayList.toArray(new String[argStringArrayList.size()]);
	}



	private InputStream extractFile(String zipfilename, String filename) {
  	final String METHOD_NAME = "extractFile(zipfilename,filename)";
	    InputStream ins = null;
	    try {
	        ZipFile zf = new ZipFile(zipfilename);
	        if (null != zf) {
	            ZipEntry ze = zf.getEntry(filename);
	            if (null != ze) {
	                ins = zf.getInputStream(ze);
	            }
	        }
	    }
	    catch (Exception e) {
        logError(CLASS_NAME, METHOD_NAME, e);
        throw new PlutoAdminException(e);
	    }
	    return ins;
	}



	//[-addToEntityReg <app-id> [<portlet-id>:<portlet-name>]+]
	private ArrayList createDeploymentArgs(String serverFileName, String tempDir, ActionRequest request, boolean appExists) throws Exception {
  	final String METHOD_NAME = "createDeploymentArgs(serverFileName,tempDir,request)";
  	Properties props = PlutoAdminContext.getInstance().getProperties();
    final String TOMCAT_HOME =  PlutoAdminContext.getInstance().getTomcatHome();
    final String PLUTO_CONTEXT =  props.getProperty("pluto-web-context");
    final String PORTLET_DEPLOY_DIR = props.getProperty("portlet-deploy-dir");
    logDebug(METHOD_NAME, "Tomcat home: " + TOMCAT_HOME);
    logDebug(METHOD_NAME, "Pluto web context: " + PLUTO_CONTEXT);

    ArrayList  args = new ArrayList();
    args.add(TOMCAT_HOME + PlutoAdminConstants.FS + "webapps");
    args.add(PLUTO_CONTEXT);
    args.add(tempDir + PlutoAdminConstants.FS + serverFileName);
    args.add(TOMCAT_HOME + PlutoAdminConstants.FS + PORTLET_DEPLOY_DIR);
    String appId = PortletRegistryService.getNextAppId();
    //check if a record in portletentityregistry exists
    if (!appExists) {
	    args.add("-addToEntityReg");
	    args.add(appId);
    }

    //Add Map of portlet name/values to session
    // to be used in drop downs on page layout page
    Map pmap = new HashMap();
    InputStream ins = extractFile(tempDir + PlutoAdminConstants.FS + serverFileName, "WEB-INF/portlet.xml");
    if (null != ins) {
	    ArrayList names = PortletNameFinder.getPortletNames(ins);
	    for (int i = 0; i < names.size(); i++) {
	      //check if a record in portletentityregistry exists
	      if (!appExists) {
	      	args.add(i+":"+names.get(i));
	      }
	      pmap.put(names.get(i), appId+"." +i);
	    }
	    ins.close();
    } else {
    	String msg = "Input stream is null";
    	PlutoAdminException e = new PlutoAdminException(msg);
    	logError(METHOD_NAME, e);
    	throw e;
    }
    request.getPortletSession().setAttribute(PlutoAdminConstants.PORTLET_MAP_ATTR, pmap);
    return args;
	}


	public static String mapToEntrySetString(Map inputMap) {
    StringBuffer sb = new StringBuffer();
    Set es = inputMap.entrySet();
    Iterator it = es.iterator();
    sb.append("Number of entries: " +  es.size());
    for (int i = 0; i < es.size(); i++) {
        Map.Entry entry = (Map.Entry) it.next();
        sb.append((String) entry.getKey().toString());
        sb.append(entry.getValue().toString());
        sb.append("\n");
    }
    return sb.toString();
	}

	/**
	 * Sets the page information into a PageTO object that is loaded into the
	 * session.
	 *
	 * @param req
	 */
	public void setPage(ActionRequest req) {
		final String METHOD_NAME = "setPage(request)";
		logMethodStart(METHOD_NAME);
		PageTO page = (PageTO)req.getPortletSession().getAttribute(PlutoAdminConstants.PAGE_ATTR);
		if (page == null) {
			page = new PageTO();
		}
		String title = req.getParameter("title");
		logDebug(METHOD_NAME, "Title: " + title);
		page.setTitle(title);
		String desc = req.getParameter("description");
		logDebug(METHOD_NAME, "Description: " + desc);
		page.setDescription(desc);
		String rows = req.getParameter("numrows");
		logDebug(METHOD_NAME, "Row count: " + rows);
		page.setRows(Integer.parseInt(rows));
		String cols = req.getParameter("numcols");
		logDebug(METHOD_NAME, "Col count: " + cols);
		page.setCols(Integer.parseInt(cols));
		req.getPortletSession().setAttribute(PlutoAdminConstants.PAGE_ATTR, page);
		logDebug(METHOD_NAME, "New page: " + page);
		logMethodEnd(METHOD_NAME);
	}
	public void savePageLayout(ActionRequest req) {
		final String METHOD_NAME = "savePageLayout(request)";
		logMethodStart(METHOD_NAME);
		//get current page
		PageTO page = (PageTO)req.getPortletSession().getAttribute(PlutoAdminConstants.PAGE_ATTR);
		logDebug(METHOD_NAME, "PageTO from session: " + page);
  	List list = new ArrayList();
		int rows = page.getRows();
		int cols = page.getCols();
    for (int i = 1; i <= rows ; i++) {
      for (int j = 1; j <= cols ; j++) {
      	String portletParam = "portlet" + i + "." + j;
      	String name_val = req.getParameter(portletParam);
      	//portlet name and values are separated by an underscore
      	int underscore = name_val.lastIndexOf("_");
      	String name = name_val.substring(0, underscore);
      	String val = name_val.substring(underscore + 1);

      	//create a PortletTO and add it to the list
      	PortletTO nPortlet = new PortletTO();
      	nPortlet.setName(name);
      	nPortlet.setValue(val);
      	nPortlet.setRow(i);
      	nPortlet.setCol(j);
      	list.add(nPortlet);
      }
    }
    page.setPortlets(list);
		logDebug(METHOD_NAME, "Updated PageTO: " + page);

		addToPageReg(page);
		logMethodEnd(METHOD_NAME);
	}

	/**
	 * Add a new page record to the pageregistry.xml file.
	 * @param page The new page to add
	 */
  public void addToPageReg(PageTO page) {
		final String METHOD_NAME = "addToPageReg(PageTO)";
		logMethodStart(METHOD_NAME);
		RandomAccessFile ras = null;

//		int rows = page.getRows();
		int cols = page.getCols();
		String name = page.getName();
	  try {
			  	//get path to pageregistry.xml
			String pageregpath = PlutoAdminContext.getInstance().getPageRegistryPath();
			//String pageregpath = "/pluto-1.0.1/webapps/pluto/WEB-INF/data/pageregistry.xml";

			File file = new File(pageregpath);
			ras = new RandomAccessFile(file, "rw");
			long length = ras.length();
			byte[] contentByte = new byte[(int) length];
			ras.read(contentByte);
			String contentString = new String(contentByte);
			//Check for previous deployment in pageregistry.xml
			String prev = "fragment name=\"" + name;
			if (contentString.lastIndexOf(prev) != -1){
				String errMsg = "Portlet '" + name + "' already exists in pageregistry.xml";
				PlutoAdminException e = new PlutoAdminException(errMsg);
				logError(METHOD_NAME, errMsg, e);
				throw e;//throw exception here
			}
			//start before close of root element
			long pos = contentString.lastIndexOf("</portal>");
			ras.seek(pos);

			//start page fragment
			ras.writeBytes("    <fragment name=\"" + name + "\" type=\"page\" >" + PlutoAdminConstants.LS);
			ras.writeBytes("        <navigation>" + PlutoAdminConstants.LS);
			ras.writeBytes("	        <title>" + page.getTitle());
			ras.writeBytes("</title>" + PlutoAdminConstants.LS);
			ras.writeBytes("	        <description>" + page.getDescription());
			ras.writeBytes("</description>" + PlutoAdminConstants.LS);
			ras.writeBytes("        </navigation>" + PlutoAdminConstants.LS);

			//iterate through portlets
			List portlets = page.getPortlets();
			//Sort list using Comparable implementation in PortletTO. This makes sure
			//	the items in the list are ordered by rows
			Collections.sort(portlets);
			Iterator iter = portlets.iterator();
			int count = 0;
			int currRow = 0;
			int lastRow = 0;
			int currCol = 0;
			while (iter.hasNext()) {
					count++;
					PortletTO portlet = (PortletTO)iter.next();
					logDebug(METHOD_NAME, "Portlet: " + portlet);
					currRow = portlet.getRow();
					currCol = portlet.getCol();
					//start row fragment
					//	Add row fragment when row changes
					if (currRow != lastRow) {
						ras.writeBytes("          <fragment name=\"row" + currRow + "\" type=\"row\">" + PlutoAdminConstants.LS);
						ras.writeBytes("             <fragment name=\"col" + count + "\" type=\"column\">" + PlutoAdminConstants.LS);
					}

						ras.writeBytes("                  <fragment name=\"p" + count + "\" type=\"portlet\">" + PlutoAdminConstants.LS);
						ras.writeBytes("                    <property name=\"portlet\" value=\"" + portlet.getValue() + "\"/>" + PlutoAdminConstants.LS);
						ras.writeBytes("                  </fragment><!-- end of portlet frag -->" + PlutoAdminConstants.LS);

						//end row fragment
						if (cols == currCol) {
							ras.writeBytes("             </fragment><!-- end of col frag -->" + PlutoAdminConstants.LS);
							//end of column iteration
							ras.writeBytes("         </fragment><!-- end of row frag -->" + PlutoAdminConstants.LS);
						}
					lastRow = currRow;
			}

			//end page fragment
			ras.writeBytes("    </fragment><!-- end of 'page' frag -->" + PlutoAdminConstants.LS);
			//add a couple of newlines to separate records
			ras.writeBytes(PlutoAdminConstants.LS);
			ras.writeBytes(PlutoAdminConstants.LS);
			//replace closing root element
			ras.writeBytes("</portal>" + PlutoAdminConstants.LS);

		} catch (IOException e) {
				logError(METHOD_NAME, e);
				throw new PlutoAdminException(e);
		} finally {
			if (ras != null) {
				try {
					ras.close();
				} catch (IOException e) {
					logError(METHOD_NAME, e);
				}
			}
		}
		logMethodEnd(METHOD_NAME);
  }

  public void addToPortletContexts(String context) {
  	final String METHOD_NAME = "addToPortletContexts(context)";
  	logParam(METHOD_NAME, "context", context);
  	String path = PlutoAdminContext.getInstance().getPortletContextsPath();
  	logDebug(METHOD_NAME, "portletcontexts.txt path: " + path);
  	File file = new File(path);
  	if (file.exists()) {
//				String fileContents = FileUtils.readFileToString(file, PlutoAdminConstants.ENCODING);
			String fileContents = readFileToString(file);
	  	logDebug(METHOD_NAME, "portletcontexts.txt contents: " + fileContents);

			//check to see whether the context already is found in the file
			if (fileContents.indexOf(context) == -1) {
				logDebug(METHOD_NAME, "Writing new context");
				StringBuffer buf = new StringBuffer(fileContents);
				buf.append(PlutoAdminConstants.LS);
				buf.append("/");
				buf.append(context);
//					FileUtils.writeStringToFile(file,buf.toString(),PlutoAdminConstants.ENCODING);
				writeStringToFile(file,buf.toString());
			}
  	} else {
			logWarn(METHOD_NAME, "File portletcontexts.txt cannot be found! You must be using Release Candidate 1.");
  	}
  }


  public boolean pageExists(String pageName) {
  	final String METHOD_NAME = "pageExists(pageName)";
  	boolean exists = true;
  	try {
			PageRegistryXao xao = new PageRegistryXao();
			exists = xao.pageExists(pageName);
		} catch (Exception e) {
			logError(METHOD_NAME, e);
			throw new PlutoAdminException(e);
		}
  	return exists;
  }

  public String readFileToString(File file){
  	final String METHOD_NAME = "readFileToString(path)";
  	String contents = null;
		FileInputStream fis = null;
  	try {
			fis = new FileInputStream(file);
			int c;
			char b;
			StringBuffer sb = new StringBuffer();
			while((c = fis.read()) != -1) {
				b = (char)c;
				sb.append(b);
			}
			contents = sb.toString().trim();
		} catch (FileNotFoundException e) {
			logError(METHOD_NAME, e);
			throw new PlutoAdminException(e);
		} catch (IOException e) {
			logError(METHOD_NAME, e);
			throw new PlutoAdminException(e);
		}	finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logError(METHOD_NAME, e);
					throw new PlutoAdminException(e);
				}
			}
		}
  	return contents;
  }

  public void writeStringToFile(File file, String contents){
  	final String METHOD_NAME = "addFileToStringToFile(contents)";
		FileOutputStream fos = null;
  	try {
			fos = new FileOutputStream(file);
			byte[] bytes = contents.getBytes();
			fos.write(bytes);
		} catch (FileNotFoundException e) {
			logError(METHOD_NAME, e);
			throw new PlutoAdminException(e);
		} catch (IOException e) {
			logError(METHOD_NAME, e);
			throw new PlutoAdminException(e);
		}	finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logError(METHOD_NAME, e);
					throw new PlutoAdminException(e);
				}
			}
		}
  }

}