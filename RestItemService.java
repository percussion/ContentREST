/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentResult;
import org.springframework.stereotype.Service;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.pso.restservice.model.Child;
import com.percussion.pso.restservice.model.ChildRow;
import com.percussion.pso.restservice.model.Copy;
import com.percussion.pso.restservice.model.DateValue;
import com.percussion.pso.restservice.model.Error;
import com.percussion.pso.restservice.model.Field;
import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.model.ItemRef;
import com.percussion.pso.restservice.model.Relationship;
import com.percussion.pso.restservice.model.Relationships;
import com.percussion.pso.restservice.model.Slot;
import com.percussion.pso.restservice.model.SlotItem;
import com.percussion.pso.restservice.model.StringValue;
import com.percussion.pso.restservice.model.Translation;
import com.percussion.pso.restservice.model.Value;
import com.percussion.pso.restservice.model.XhtmlValue;
import com.percussion.pso.restservice.model.Error.ErrorCode;
import com.percussion.pso.restservice.model.results.PagedResult;
import com.percussion.pso.restservice.support.IImportItemSystemInfo;
import com.percussion.pso.restservice.support.ImportItemSystemInfoLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.memory.PSCacheAccessUtils;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.PSUserNotMemberOfCommunityException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
/**
 * Business layer package service that calls to the lower level CRUD package
 * service allowing exposure to clients via "web services" which in this case is
 * REST.
 * 
 * @author erikserating
 * 
 */


@Service(value = "restItemService")
@Path("Content")
@ProduceMime("text/xml")
public class RestItemService implements ExternalItemService
{
	IImportItemSystemInfo sysinfo = null;
	private  Map<Long,String> contentTypeNameMap = new HashMap<Long,String>();
	private  Map<Integer,String> communityNameMap = new HashMap<Integer,String>();
	private  Map<Integer,String> siteNameMap = new HashMap<Integer,String>();
	private  Map<Integer,String> slotNameMap = new HashMap<Integer,String>();
	private  Map<Integer,String> templateNameMap = new HashMap<Integer,String>();
	private  IPSGuidManager gmgr = null;
	private  IPSContentWs cws=null;
	private  IPSCmsContentSummaries summ = null; 
	private  IPSAssemblyService aService = null;
	private  IPSSystemWs system = null;
	private  IPSSiteManager sitemgr = null;
	private  IPSBackEndRoleMgr rolemgr = null;
	private  IPSWorkflowService wf = null;
	private  IPSContentMgr contentMgr = null;
	private  IPSFilterService filter;
	private  final int PAGE_SIZE=1000;
	/**
	 * Logger for this class
	 */
	private static final Log log = LogFactory.getLog(RestItemService.class);
	

	
	
	private UriInfo uri;


	public void setUriInfo(UriInfo uri) {
		this.uri = uri;
	}


	private void initServices()
	{

		if(gmgr == null)
		{
			sysinfo = ImportItemSystemInfoLocator.getImportItemSystemInfo();
			gmgr = PSGuidManagerLocator.getGuidMgr(); 
			cws = PSContentWsLocator.getContentWebservice();
			summ = PSCmsContentSummariesLocator.getObjectManager();
			system = PSSystemWsLocator.getSystemWebservice();
			rolemgr = PSRoleMgrLocator.getBackEndRoleManager();
			contentMgr = PSContentMgrLocator.getContentMgr();
			aService = PSAssemblyServiceLocator.getAssemblyService();
			sitemgr = PSSiteManagerLocator.getSiteManager();
			wf = PSWorkflowServiceLocator.getWorkflowService();
			filter = PSFilterServiceLocator.getFilterService();
			
			List<PSContentTypeSummary> ctypes = cws.loadContentTypes(null);
			for (PSContentTypeSummary type : ctypes) {
				contentTypeNameMap.put(type.getGuid().longValue(), type.getName()); 
			}
		}
	}


	public PagedResult getItems(Integer n)
	{
		return jcrSearch("select rx:sys_contentid from nt:base ",n,"/");
	}
	
	
	public PagedResult getItems(String path,Integer n)
	{
		return jcrSearch("select rx:sys_contentid from nt:base ",n,"/Sites/"+path);
	}
	
	
	public PagedResult getFolders(Integer n)
	{
		return jcrSearch("select rx:sys_contentid from rx:folder ",n,"/Folders/");
	}

	private PagedResult jcrSearch(String q, Integer n,String path) {
		initServices();
		Map<String,String> pmap = new HashMap<String,String>();
		PagedResult resultPage = new PagedResult();
		List<ItemRef> refs = new ArrayList<ItemRef>();
		Set<Integer> ids = new TreeSet<Integer>();
		Query query;
		if (n==null) n=1;

		String where="";
		RowIterator rows=null;



		try {
			boolean moreResults=true;
			while (moreResults && ids.size()<PAGE_SIZE) {
				where="";
				if (n>1) {
					where =" where rx:sys_contentid >"+n.toString();
				}
				if (path!= null && path.length()>1 && path.startsWith("/")) {
					where += " and jcr:path like '//"+path+"%";
				}
				q+=where+" order by rx:sys_contentid";
				log.debug("Starting query "+q);
				query = contentMgr.createQuery(q, Query.SQL);
				
				QueryResult qresults = contentMgr.executeQuery(query, PAGE_SIZE+10, pmap);
				log.debug("Query returned ");
				rows = qresults.getRows();
				int rowcount = 0;
				while(rows.hasNext() && ids.size()<PAGE_SIZE){
					rowcount++;
					Row row = rows.nextRow();	
					String contentid = row.getValue("rx:sys_contentid").getString();
					int id = Integer.valueOf(contentid);
					ids.add(id);
					n=id;	
				}
				if (rowcount<PAGE_SIZE) moreResults=false;
			}

			if (ids.size()==PAGE_SIZE && moreResults) {
				resultPage.setNext(generatePagedLink("Rhythmyx/services/Content"+path, n));
			}

			for(Integer id : ids) {
				ItemRef ref = new ItemRef();
				ref.setContentId(id);
				ref.setHref(generateItemLink(id, -1));
				refs.add(ref);
			}
			resultPage.setItemRefs(refs);
		} catch (InvalidQueryException e) {
			log.error(e);
		} catch (RepositoryException e) {
			log.error(e);
		}
		return resultPage;	
	}
	@GET
	@Path("{id}")
	public Item getItem(@PathParam("id") int id)
	{
		initServices();
		return getFromRxItem(id,-1);
	}

	@GET
	@Path("{id}/{rev}")
	public Item getItemRev(@PathParam("id") int id,@PathParam("rev") int rev)
	{
		initServices();
		return getFromRxItem(id,rev);
	}

	private Item getFromRxItem(int id,int rev) {

		Item item = new Item();
		item.setContentId(id);
		item.setRevision(rev);
		try {

			PSComponentSummary summary = summ.loadComponentSummary(id);
			if (summary == null) {
				item.addError(ErrorCode.NOT_FOUND);
			} else {


				if (rev == -1) {
					// need to retun error if content id does not exist summary is null.
					rev = summary.getHeadLocator().getRevision();
				}
				List<IPSGuid> guids = Collections.singletonList(gmgr.makeGuid(new PSLocator(id,rev)));
				String community = getCommunityName(summary.getCommunityId());
			
				Node node = contentMgr.findItemsByGUID(guids, null).get(0);

				int contenttypeid = Integer.valueOf(node.getProperty("rx:sys_contenttypeid").getString());


				String contentTypeName = getContentTypeName(contenttypeid);
				PSItemDefinition itemdef = getItemDefinition(contentTypeName);

				item.setFields(getFromRxFields(node, itemdef));
				item.setChildren(getFromRxChildren(node, itemdef));
				item.setContentId(id);
				item.setContentType(contentTypeName);
				item.setCommunityName(community);
				item.setRevision(rev);
				List<String> folderPaths = Arrays.asList(cws.findFolderPaths(guids.get(0)));
				item.setFolders(folderPaths);
				item.setLocale(summary.getLocale());
				item.setRelationships(getRelationships(id, rev,true));
				item.setDepRelationships(getRelationships(id, rev,false));
				int stateid = summary.getContentStateId();
				int workflowid = summary.getWorkflowAppId();
				log.debug("State  id = "+stateid);
				log.debug("Workflow id = "+workflowid);
				if(workflowid > 0) {
					item.setWorkflow(getWorkflowName(workflowid));
					item.setState(getStateName(workflowid, stateid));
				}

			}
		} catch (PSErrorException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error",e);
		} catch (RepositoryException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error",e);
		} catch (PSInvalidContentTypeException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error",e);
		} catch (Exception e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error",e);
		} 

		return item;
	}



	private String getWorkflowName(int id) {
		log.debug("Getting workflow name");
		PSWorkflow workflow = wf.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW,id));
		log.debug("got workflow name "+workflow.getName());
		return workflow.getName();
	}
	private String getStateName(int wfid,int stateid) {
		log.debug("Getting state name");
		PSState state = wf.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,stateid),new PSGuid(PSTypeEnum.WORKFLOW,wfid));
		log.debug("Got state name"+state.getName());
		return state.getName();
	}


	private String getSiteName(int id) {
		log.debug("Getting site name");

		String siteName=String.valueOf(id);
		if (siteNameMap.containsKey(id)) {
			siteName=siteNameMap.get(id);
		} else {

			IPSSite site=null;
			try {
				site = sitemgr.loadSite(new PSGuid(PSTypeEnum.SITE, id));
				siteName=site.getName();
				siteNameMap.put(id, siteName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Cannot load site", e);
			}
		}
		return siteName;

	}

	private String getFolderPath(int id) {
		log.debug("Getting folder path");
		try {
			List<PSFolder> folderList = cws.loadFolders(Collections.singletonList(gmgr.makeGuid(new PSLocator(id,-1))));
			log.debug("Got folder Path");
			return folderList.get(0).getFolderPath();
		} catch (PSErrorResultsException e) {
			log.error("Cannot get folder path for foder id "+id);
		} 

		return null;
	}

	private String getSlotName(int id) {
		log.debug("Getting slot name");
		String slotname=slotNameMap.get(id);
		if (slotname==null) {
			IPSTemplateSlot slot;
			try {
				slot = aService.loadSlot(new PSGuid(PSTypeEnum.SLOT, id));
				slotname=slot.getName();
			} catch (Exception e) {
				log.debug("Cannot load slot "+id,e);
				slotname=String.valueOf(id);
			}


		}
		return slotname;
	}

	private String getTemplateName(int id) {
		log.debug("Getting template name");
		String templatename=templateNameMap.get(id);

		if (templatename==null) {
			try{
				IPSAssemblyTemplate template =  aService.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, id),false);
				templatename=template.getName();
			} catch (PSAssemblyException e) {
				log.error("cannot get template for id "+id,e);
				templatename=String.valueOf(id);
			}

		}

		IPSAssemblyTemplate template=null;
		try {
			template = aService.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, id),false);
		} catch (PSAssemblyException e) {
			log.error("Cannot find template with id "+id);
		}
		log.debug("Got template name");
		return (template==null) ? Integer.toString(id) : template.getName();
	}
	private String getContentTypeName(long id) {
		return(contentTypeNameMap.get(id));  
	}
	private String getCommunityName(int id) {

		String name=null;
		if (id > 0) {
			if (!communityNameMap.containsKey(id)) {
				PSCommunity[] communities = rolemgr.loadCommunities(new IPSGuid[]
				                                                                {new PSGuid(PSTypeEnum.COMMUNITY_DEF, id)});
				if (communities.length==1) {
					communityNameMap.put(id, communities[0].getName());
					name=communities[0].getName();
				} else {
					name = Integer.toString(id);
				}
			} else {
				name=communityNameMap.get(id);
			}}
		return name;
	}

	private Field convertPSFieldtoField(Property field, Node parentNode, PSField psfield,PSItemDefinition itemdef, boolean child) throws RepositoryException {



		Field newField = new Field();


		// Get definition fails for child field need some better way of detecting multi valued
		boolean isMultiValue = (child)? false : field.getDefinition().isMultiple();


		boolean isBinary = psfield.getDataType().equals("binary");

		newField.setName(field.getName().substring(3));				
		if (isMultiValue) {
			List<Value> values = new ArrayList<Value>();
			for (javax.jcr.Value value : Arrays.asList(field.getValues())) {
				values.add(toXmlValue(value));
			}
			newField.setValues(values);
		} else if (isBinary) { 
			String contentid = parentNode.getProperty("rx:sys_contentid").getString();
			String revisionid = parentNode.getProperty("rx:sys_revision").getString();
			log.debug("Editor URL "+ itemdef.getEditorUrl());
			String href = this.uri.getBaseUri()+"Rhythmyx/"+itemdef.getAppName()+"/"
			+itemdef.getContentEditor().getName()+"?sys_command=binary&sys_contentid="
			+contentid+"&sys_revision="+revisionid
			+"&sys_submitname="+field.getName()
			+"&sys_childrowid=";
			log.debug("href="+href);
			StringValue binary = new StringValue();
			binary.setHref(href);
			newField.setValue(binary);
		} else {
			newField.setValue(toXmlValue(field.getValue()));
		}		

		return newField;
	}
	private List<Field> getFromRxFields(Node item, PSItemDefinition itemdef) {
		List<Field> fields = new ArrayList<Field>();

		Iterator<PSField> iterator = itemdef.getParentFields();

		while(iterator.hasNext()) {
			try{
				PSField psfield = iterator.next();
				String type = psfield.getDataType();
				String fieldname=psfield.getSubmitName();
				if (type.equals("binary") || item.hasProperty("rx:"+fieldname)){
					Property field = item.getProperty("rx:"+fieldname);
					Field newField = convertPSFieldtoField(field, item, psfield, itemdef,false);
					fields.add(newField);
				}
			} catch (RepositoryException e) {
				log.error(e,e);
			}
		}		
		return fields;
	}


	@SuppressWarnings("unchecked")
	List<Child> getFromRxChildren(Node item, PSItemDefinition itemdef) {
		List<Child> children = new ArrayList<Child>();

		for(PSFieldSet child : itemdef.getComplexChildren()) {
			try {
				Child newChild = new Child();
				children.add(newChild);

				List<ChildRow> rows = new ArrayList<ChildRow>();

				log.debug("Adding child field set "+child.getName());
				newChild.setName(child.getName());
				newChild.setRows(rows);
				NodeIterator ni;

				ni = item.getNodes(child.getName());


				while (ni.hasNext()) {
					log.debug("Adding child row");
					Node n = ni.nextNode();

					ChildRow cr = new ChildRow();
					rows.add(cr);
					List<Field> fields = new ArrayList<Field>();
					cr.setFields(fields);
					Iterator<PSField> iterator = child.getEveryField();

					while(iterator.hasNext()) {

						try{
							PSField psfield = iterator.next();
							String type = psfield.getDataType();
							String fieldname=psfield.getSubmitName();
							log.debug("adding child field "+fieldname);
							if (type.equals("binary") || n.hasProperty("rx:"+fieldname)){
								Property field = n.getProperty("rx:"+fieldname);
								Field newField = convertPSFieldtoField(field, n, psfield, itemdef,true);
								fields.add(newField);
							}
						} catch (RepositoryException e) {
							log.error(e,e);
						}
					}
				}

			} catch (RepositoryException e) {
				log.debug(e);
			}
		}
		return children;

	}
	private Value toXmlValue( javax.jcr.Value oldValue) {
		Value newValue=null;
		log.debug("Value type is "+oldValue.getType());
		try {

			if (oldValue.getString().contains("class=\"rxbodyfield\"")) {

				newValue = new XhtmlValue();

				newValue.setStringValue(oldValue.getString());
			} else if (oldValue.getType() == PropertyType.DATE) {
				DateValue dateValue = new DateValue();
				dateValue.setDate(oldValue.getDate().getTime());
				newValue = dateValue;
			} else {
				newValue = new StringValue();
				newValue.setStringValue(oldValue.getString());
			}

		} catch (ValueFormatException e) {
			log.error(e);
		} catch (IllegalStateException e) {
			log.error(e);
		} catch (RepositoryException e) {
			log.error(e);
		}
		return newValue;
	}



	private Relationships getRelationships(int id,int rev,boolean isOwner) {
		Relationships xmlRels = new Relationships();
		PSRelationshipFilter filter = new PSRelationshipFilter();
		List<Translation> translations = xmlRels.getTranslations();
		List<Copy> copies = xmlRels.getCopies();
		List<Slot> slots = xmlRels.getSlots();

		if (isOwner) {
			if (rev<=0) {
				filter.setOwnerId(id);
				filter.limitToEditOrCurrentOwnerRevision(true);
			} else {
				filter.setOwner(new PSLocator(id,rev));

			}
		}
		else {
			filter.setDependent(new PSLocator(id,-1));
			filter.getLimitToEditOrCurrentOwnerRevision();
			filter.limitToEditOrCurrentOwnerRevision(true);
		}
		try {
			List<PSRelationship> rels = system.loadRelationships(filter);
			log.debug("found "+rels.size()+" relationships");
			for (PSRelationship rel : rels) {
				String category =rel.getConfig().getCategory();
				String name = rel.getConfig().getName();

				PSLocator dependent = rel.getDependent();
				PSLocator owner = rel.getOwner();
				int dependentId = dependent.getId();
				int cid =  isOwner ? dependent.getId() : owner.getId();
				int revision = isOwner ? dependent.getRevision() : owner.getRevision();
				log.debug("category = "+category);
				log.debug("Name = "+name);
				log.debug("dependentid="+dependentId);
				log.debug("ownerid="+dependentId);
				log.debug("revision = "+revision);

				Map<String,String> props = rel.getAllProperties();
				for(Map.Entry<String,String> entry : props.entrySet()) {
					log.debug("Prop name="+entry.getKey()+" value="+entry.getValue());
				}

				if (category.equals(PSRelationshipConfig.CATEGORY_TRANSLATION)) {
					PSComponentSummary summary = summ.loadComponentSummary(cid);
					String locale = summary.getLocale();
					Translation trans = new Translation();
					trans.setLocale(locale);
					trans.setContentId(cid);
					trans.setHref(generateItemLink(cid, revision));
					trans.setRelId(rel.getId());
					trans.setRevision(revision);

					if (translations == null ) {
						translations = new ArrayList<Translation>();
					}
					translations.add(trans);
				} else if (category.equals(PSRelationshipConfig.CATEGORY_COPY)) {

					Copy copy = new Copy();
					copy.setContentId(cid);
					copy.setRelId(rel.getId());

					copy.setRevision(revision);
					copy.setHref(generateItemLink(cid, revision));
					if (copies == null ) {
						copies = new ArrayList<Copy>();
					}
					copies.add(copy);
				} else if (category.equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)) {
					log.debug("This is an AA Relationship");
					SlotItem newSlotItem = new SlotItem();
					String slotid = props.get("sys_slotid");

					String templateid = props.get("sys_variantid");
					String siteid = props.get("sys_siteid");
					String sortrank = props.get("sys_sortrank");
					String folderid = props.get("sys_folderid");

					newSlotItem.setContentId(cid);
					newSlotItem.setRelId(rel.getId());

					newSlotItem.setRevision(revision);
					newSlotItem.setHref(generateItemLink(cid, revision));
					if (templateid != null) {
						newSlotItem.setTemplate(getTemplateName(Integer.parseInt(templateid)));
					}

					if (siteid != null) {
						newSlotItem.setSite(getSiteName(Integer.parseInt(siteid)));
					}

					if (folderid != null) {
						newSlotItem.setFolder(getFolderPath((Integer.parseInt(folderid))));
					}
					if (sortrank != null) {
						newSlotItem.setSortRank(Integer.parseInt(sortrank));
					}
					String slotname=null;
					if (slotid != null) {
						slotname=getSlotName(Integer.parseInt(slotid));

						log.debug("Slotname is "+slotname);
						Slot slot = new Slot();
						slot.setName(slotname);
						if (slots==null) slots = new ArrayList<Slot>();


						if (!slots.contains(slot)) {
							slot.setType(rel.getConfig().getName());
							slots.add(slot);
							log.debug("cannot find slot" +slot.getName());
						} else {
							log.debug("slot already exists");
							slot = slots.get(slots.indexOf(slot));
							log.debug("Got existing slot"+slot.getName());
						}
						if (slot != null) {
							List<SlotItem> items = slot.getItems();
							if (items==null) items = new ArrayList<SlotItem>();
							log.debug("Adding new slot item to list");
							items.add(newSlotItem);
							//Better to do all sorting in one go
							Collections.sort(items);
							slot.setItems(items);
						}
					}

				}
			}

			xmlRels.setCopies(copies);
			xmlRels.setSlots(slots);
			xmlRels.setTranslations(translations);

		} catch (PSErrorException e) {
			log.error(e);
		}
		return xmlRels;

	}
	public String generateItemLink(int contentid, int revision) {
		//TODO Get base path
		String uri;
			uri="http://localhost:9992/Rhythmyx/services/Content/"+contentid
			+"/"+revision;
					return uri ;
	}

	public String generatePagedLink(String path, int next) {
		UriBuilder builder = this.uri.getBaseUriBuilder();
		builder.path(path);
		builder.queryParam("n", Integer.valueOf(next).toString());
		return builder.build().toASCIIString(); 
	}

	private PSItemDefinition getItemDefinition(String contentType)
	throws PSInvalidContentTypeException
	{
		PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

		return itemDefMgr.getItemDef(contentType,PSItemDefManager.COMMUNITY_ANY);
	}


	/*
	 * Updates 
	 * 
	 */

	/* import template */
	@POST
	@Path("import/{template}")
	public Response updateItem( @PathParam("template") String templateName) {
		initServices();
		log.debug("Import template is "+templateName);
		PSRequest req2 =  (PSRequest) PSRequestInfo
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		HttpServletRequest req = req2.getServletRequest();
		
		Item item = new Item();
		IPSAssemblyItem asmItem = new PSAssemblyWorkItem();
		String output="";
		DOMReader reader = new DOMReader();
		try {
			
			IPSAssemblyTemplate template = aService.findTemplateByName(templateName);
		

			Map<String,Object> bindings = new HashMap<String,Object>();
			/*
			req.getInputStream();
			
			Tidy tidy = new Tidy();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			
			
			Document doc = reader.read(tidy.parseDOM(req.getInputStream(), os));
			
			
			log.debug("Tidy output : "+os.toString("UTF-8"));
			
			bindings.put("$import.source",doc);
			bindings.put("$import.item",item);
		*/
			asmItem.setParameterValue("sys_itemfilter", "preview");
			asmItem.setParameterValue("sys_template", String.valueOf(template.getGUID().getUUID()));
			asmItem.setParameterValue("sys_contentid", "1");
			PSLegacyGuid guid = new PSLegacyGuid(new PSLocator(1,-1));
			asmItem.setId(guid);
			asmItem.setFilter(filter.findFilterByName("preview"));
			asmItem.setBindings(bindings);
			asmItem.setTemplate(template);
			asmItem.setParameters(req.getParameterMap());
			
			List<IPSAssemblyResult> asmResult = aService.assemble(Collections.singletonList(asmItem));
			
			output = new String(asmResult.get(0).getResultData(),template.getCharset());
			
			/*
			PSAssemblyJexlEvaluator res = new PSAssemblyJexlEvaluator(asmItem);
			BInputStream is = new BufferedInputStream();
			processBindings(asmItem, res);
		
			Object importItem = asmItem.getBindings().get("$import.item");
			if (importItem != null && importItem instanceof Item) {
				item = (Item) importItem;
			} else {
				log.debug("$import.item returned "+importItem);
			}
			
			importItem = asmItem.getBindings().get("$import.item");
			if (importItem != null && importItem instanceof Item) {
				item = (Item) importItem;
			} else {
				log.debug("$import.item returned "+importItem);
			}
			*/
			
		} catch (PSAssemblyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PSFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PSTemplateNotImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		/*
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.putAll(asmItem.getBindings());
	
		XStream xstream = new XStream();
		String xml = xstream.toXML(map);
		
		xstream.registerConverter(new MapConverter(xstream.getMapper()));
		log.debug("XStream:" + xml);
		*/
	
		return  Response.ok(output).build();
		
	}

	
	   public Document createOutputDocument(Document doc, List<Object> bindingRefs) {
		   Document sourceDoc = null;
		   
	        Document document = DocumentHelper.createDocument();
	        Element root = document.addElement( "ImportSource" );

	        root.addElement( "doc" ).add(sourceDoc);
	        Element refs = root.addElement("refs");
	        
	        for (Object ref : bindingRefs) {
	        
	        		Document refDoc;
					try {
						refDoc = DocumentHelper.parseText( ref.toString() );
						refs.add(refDoc.getRootElement());
					} catch (DocumentException e) {
					    log.debug("cannot parse reference doc "+ ref.toString() );
					}
	     
	        }
	        
	      
	        return document;
	    }

	
	@POST
	@Path("{id}")
	@ConsumeMime("text/xml")
	public Response updateItem( @PathParam("id") int id, Item item) {
		log.debug("Id referenced from path is "+id);
		if (item.getContentId() == null) {
			item.setContentId(id);
		} else if ( item.getContentId().intValue() != id ) {
			log.error("Content id from path different than content id specified in item");
			// respond with error
		}
		return  Response.ok(item).build();
	}



	@DELETE
	@Path("{id}")
	public Item purgeItem(@PathParam("id") int id) {
		Item item = new Item();
		item.setContentId(id);
		try {
			PSRequest req =  (PSRequest) PSRequestInfo
			.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
			IPSRequestContext ctx = new PSRequestContext(req);
			String userName= ctx.getUserContextInformation("User/Name", "").toString();


			String session = req.getServerRequest().getUserSession().getId();

			log.debug("Found user "+userName);
			log.debug("Found session "+session);
			initServices();


			log.debug("Deleting item "+id);
			// convert content id to guids,  revision -1 should enforce
			// current version of requests.  
			IPSGuid guid = gmgr.makeGuid(new PSLocator(id,-1));
			List<IPSGuid> guids = Collections.singletonList(guid);
			Item currentItem = getItem(id);
			boolean requireCheckout = currentItem.getContentType().equals("Folder") ? false : true;
			if (currentItem.getErrors() != null && currentItem.getErrors().size()> 0) {
				item.setErrors(currentItem.getErrors());
			}else {

				// Change to correct community.  Folders do not return community for allCommunities id =-1
				if (currentItem.getCommunityName()!=null  && currentItem.getCommunityName().length() > 0) {
					system.switchCommunity(currentItem.getCommunityName());
				}
				//Force checkin item.  checkin is ok even if it is already checked in
				//This ensures all ateims are in the correct checked in state before processing.
				//Revision may increment if it is checked out but there will be a snapshot of the item before
				//we modify it.

				if (requireCheckout) {
					log.debug("Forcing Checking in item now");
					cws.checkinItems(guids, "Forced checkin by Importer");
					cws.prepareForEdit(guids);
				}

				cws.deleteItems(guids);
				item.addError(ErrorCode.PURGED);

			}
		} catch (PSDataExtractionException e) {
			// TODO Auto-generated catch block
			log.error("Cannot Purge item",e) ;
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
		} catch (PSUserNotMemberOfCommunityException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.debug("Current user is not in community of item, ",e);
		} catch (PSErrorsException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e);
		} catch (PSErrorResultsException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e);
		} catch (PSErrorException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e);

		} catch (Exception e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e);
		}
		return item;
	}

	@POST
	@Path("/")
	@ConsumeMime("text/xml")
	public Response updateItem(Item item) {
		try {
			PSRequest req =  (PSRequest) PSRequestInfo
			.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
			IPSRequestContext ctx = new PSRequestContext(req);
			String userName= ctx.getUserContextInformation("User/Name", "").toString();


			String session = req.getServerRequest().getUserSession().getId();

			log.debug("Found user "+userName);
			log.debug("Found session "+session);
			initServices();
			int id = -1;
			log.debug("Id referenced from item is " + item.getContentId());
			if (item.getContentId() == null) {
				log.error("No content id");
				locateItem(item);
			} 
			Item currentItem =null;
			boolean requireCheckout = false;
			boolean newItem = false; 
			List<IPSGuid> guids = null;
			if (item.getContentId()!= null) {
				id = item.getContentId();
			
			// convert content id to guids,  revision -1 should enforce
			// current version of requests.  
			IPSGuid guid = gmgr.makeGuid(new PSLocator(id,-1));
			 guids = Collections.singletonList(guid);
			currentItem = getItem(id);
			
			 requireCheckout = currentItem.getContentType().equals("Folder") ? false : true;
			}  else {
				newItem=true;
			}
			//  Currently has not effect as we need to check fields also;
			//requireCheckout |= updateRelationships(item,currentItem,false);
			try { 

				// Change to correct community
				if (currentItem == null && item.getCommunityName() != null) {
					log.debug("Switching community to "+item.getCommunityName());
					system.switchCommunity(item.getCommunityName());
				} else if (currentItem.getCommunityName()!=null  && currentItem.getCommunityName().length() > 0) {
					log.debug("Switching community to "+currentItem.getCommunityName());
					system.switchCommunity(currentItem.getCommunityName());
				}
				//Force checkin item.  checkin is ok even if it is already checked in
				//This ensures all ateims are in the correct checked in state before processing.
				//Revision may increment if it is checked out but there will be a snapshot of the item before
				//we modify it.
				List<PSItemStatus> status = null;
				if (requireCheckout) {
					log.debug("Forcing Checking in item now");
					cws.checkinItems(guids, "Forced checkin by Importer");
					status = cws.prepareForEdit(guids);
					// If checked out revision will have increased,  relationship ids will change as well as workflow
					// ephox fields will have new relationship ids also
				}
				List<PSCoreItem> psItems;
				PSCoreItem  psItem=null;
			
				if (!newItem) {
				PSComponentSummary summary = summ.loadComponentSummary(id);
				int newRev = summary.getHeadLocator().getRevision();
				if (currentItem.getRevision() != newRev) {
					log.debug("Revision change afer prepareForEdit getting new revision "+newRev);
					currentItem = getItemRev(id,newRev);
				}
				
				// Now we are ready to modify item.
				// params,  include  binary,children,related,folderpath
				//  would be nice to not load item twice but we need updated revision and relationship ids.


			    psItems = cws.loadItems(guids, false, true, false,false);
				
				psItem = psItems.get(0);
				} else {
					String contentType = item.getContentType();
					if (contentType==null) {
						log.error("Content Type not specified for import create");
						//Throw exception
					}
					psItems = cws.createItems(contentType, 1);
					psItem = psItems.get(0);
				}
				log.debug("Updating fields");
				updateFields(item,psItem);

				psItem.setFolderPaths(item.getFolders());
				updateRelationships(item,currentItem,userName,false);


				// params,  enable revisions, checkin
				log.debug("Saving item ");
				cws.saveItems(psItems, false, false);


				//Release from edit

				//TODO:  do we use checkin only and separatly transition item.
				if (requireCheckout) {
					cws.releaseFromEdit(status,false);
				}


			} catch (PSUserNotMemberOfCommunityException e) {
				log.debug("Current user is not in community of item, ",e);
			} catch (PSErrorsException e) {
				log.error(e);
			} catch (PSErrorResultsException e) {
				log.error(e);
			} catch (PSUnknownContentTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			//  compare binaries (use checksum?)
			// normalize binaries with checksum.  
			// replace binary field with checksum
			// equivalnence of ephox fields?

			// filter out unchanged fields.  update remaining set have to checkout
			// filter unchanged child tables

			// Check for modified path / paths, if only added path does not require checkin

			// Slots order matters filter inline need to specify by lookup (performance)
			// other relationship types (translation)


			//  update fields
			// update child tables
			// update paths
			// update slots
			// update other rels
			
			IPSGuid guid = gmgr.makeGuid(new PSLocator(id,-1));
			PSCacheAccessUtils.evict(guid);
			
			PSCacheProxy.flushHibernateCache();
			
			Item returnedItem = getItem(id);
			log.debug("Itemservice after get "+getItemXml(returnedItem));
			return  Response.ok(returnedItem).build();

		} catch (PSDataExtractionException e) {
			// TODO Auto-generated catch block
			log.error("Cannot find user",e) ;
			return   Response.serverError().build();
		}
	}







	private void locateItem(Item item) {
		if (item.getContentId() != null && item.getContentId() > 0) {
			log.debug("Item already located, contentid is "+item.getContentId());
		} else {
			String keyField = item.getKeyField();
			if (keyField==null) {
				log.error("No Content Id or keyfield specified for item cannot locate");
				return;
			} 
			String value=null;
			for (Field field : item.getFields() ) {
				if (field.getName().equals(keyField)) {
					value = field.getValue().getStringValue();
					break;
				}
			}
			if (value==null) {
				log.error("Cannot get value for keyfield "+keyField);
				return;
			}
			String query = "select rx:sys_contentid from nt:base where rx:"+keyField+"='"+value+"'";
			
			String path=item.getContextRoot();
			
			PagedResult res = jcrSearch(query, 1, path);
			int size=res.getItemRefs().size();
			if (size == 0) {
				log.error("Cannot locate item create?");
				if (item.getUpdateType() != null && item.getUpdateType().equals("ref")) {
					log.error("Cannot locate item and item is a reference");
					item.getErrors().add(new Error(ErrorCode.NOT_FOUND, "Cannot locate keyfield "+keyField+" path="+path));
				}
			} else if (size>1) {
				log.error("Duplicate items detected for keyfield "+keyField+" path="+path);
			} else {
				int id = res.getItemRefs().get(0).getContentId();
				log.debug("located item id="+id);
				item.setContentId(id);
			}
			
			
		}
	}


	private boolean updateRelationships(Item item, Item currentItem,String userName, boolean check) {

		Relationships updateRels = item.getRelationships();
		if (updateRels!=null) {
			PSRelationshipProcessor proc = PSWebserviceUtils.getRelationshipProcessor();

			List<Slot> updateSlots = updateRels.getSlots();
			if (updateSlots != null) {
	
				//TODO: Best to calculate slot type based upon slot itself,  Currently rely on type to be specified.
				for (Slot slot : updateSlots) {
					log.debug("Updating slot "+slot.getName());
					List<SlotItem> updateItems = slot.getItems();
					List<SlotItem> existingItems = new ArrayList<SlotItem>();
					if (currentItem.getRelationships() != null 
							&& currentItem.getRelationships().getSlots() != null
							&& currentItem.getRelationships().getSlots() != null) {

						for(Slot existSlot : currentItem.getRelationships().getSlots()) {
							if(existSlot.getName().equals(slot.getName())) {
								existingItems.addAll(existSlot.getItems());
							}
						}



					}

					if (!updateItems.equals(existingItems)) {
						if (check) {
							return true;
						} else {
							//  We now know relationships have changed easiest to 
							//  Delete all and re-add due to ordering.
							PSLocator owner  = new PSLocator(currentItem.getContentId(),currentItem.getRevision());

							List<Relationship> relsToDelete = new ArrayList<Relationship>(existingItems);
							log.debug("slot type is"+slot.getType());
							deleteRelationships(owner,slot.getType(),relsToDelete);
							IPSGuid ownerGuid = gmgr.makeGuid(owner);
							for(SlotItem itemToAdd : updateItems) {
								try {
									List<IPSGuid> relGuids = Collections.singletonList(gmgr.makeGuid(new PSLocator(itemToAdd.getContentId(),-1)));
								
									// TODO:  Need to cache id lookups.
									IPSGuid folderId=null;
									log.debug("Getting slot id");
									
									IPSGuid slotId = aService.findSlotByName(slot.getName()).getGUID();
									IPSGuid siteId=null;
									IPSGuid templateId=null;
									log.debug("Getting folder id");
									if (itemToAdd.getFolder() != null) {
										List<IPSGuid>folderIds = cws.findPathIds(itemToAdd.getFolder());
										if (folderIds.size()>0) {
											// returns all parts of path,  get last for id of this folder.
											folderId=folderIds.get(folderIds.size());
										} else {
											log.error("Cannot get guid for folder " + itemToAdd.getFolder());
										}
									}
									log.debug("Getting template id");
									if (itemToAdd.getTemplate()!=null) {
										templateId = aService.findTemplateByName(itemToAdd.getTemplate()).getGUID();
									}
									log.debug("Getting site id");
									if (itemToAdd.getSite()!=null) {
										siteId = sitemgr.findSiteByName(itemToAdd.getSite()).getGUID();
									}

									log.debug("creating relationships");
									cws.addContentRelations(ownerGuid, relGuids, folderId, siteId, slotId, templateId, -1);
								}
								catch (Exception e) {
										log.debug("Cannot create relationship ", e);
								}
							}

						}
						}
					}
			}
					if (updateRels.getTranslations() != null) {
						List<Translation> updateTrans = new ArrayList<Translation>(updateRels.getTranslations());
						List<Translation> existingTrans = new ArrayList<Translation>();


						if (currentItem.getRelationships() != null 
								&& currentItem.getRelationships().getTranslations() != null ) {
							existingTrans.addAll(currentItem.getRelationships().getTranslations());
						}

						List<Relationship> itemsToDelete = new ArrayList<Relationship>(existingTrans);
						itemsToDelete.removeAll(updateTrans);
						log.debug("Need to remove "+itemsToDelete.size()+" relationships");

						List<Translation> itemsToAdd = new ArrayList<Translation>(updateTrans);
						itemsToAdd.removeAll(existingTrans);
						log.debug("Need to add "+itemsToAdd.size()+" relationships");

						//  Delete relationships but rid
						PSLocator owner  = new PSLocator(currentItem.getContentId(),currentItem.getRevision());

						if (itemsToDelete.size()>0) {
							if (check) {
								return true;
							} else {

								deleteRelationships(owner,PSRelationshipConfig.TYPE_TRANSLATION,itemsToDelete);

							}
						}
						log.debug("Adding relationships for id="+currentItem.getContentId()+" revision="+currentItem.getRevision());

						if (itemsToAdd.size()> 0) {
							if (check) {
								return true;
							} else {
								try {
									PSRelationshipConfig relconf = proc.getConfig(PSRelationshipConfig.TYPE_TRANSLATION);
									PSRelationshipSet relSet = new PSRelationshipSet();
									for (Translation trans : itemsToAdd) {
										PSLocator dependent  = new PSLocator(trans.getContentId(),-1);
										PSRelationship newRel =  new PSRelationship(-1, owner, dependent, relconf);
										//  Possibly test whether locale in Translation matches local of actual dependent
										relSet.add(newRel);
									}

									proc.save(relSet);
								} catch (PSCmsException e) {
									log.debug("Cannot add Relationships with config "+PSRelationshipConfig.TYPE_TRANSLATION );
								}
							}
						}
					}



				}
				return false;

			}

			private void deleteRelationships(PSLocator owner,String type, List<Relationship> rels) {
				PSRelationshipProcessor proc = PSWebserviceUtils.getRelationshipProcessor();

				log.debug("Deleting relationships for id="+owner.getId()+" revision="+owner.getRevision());
				int[] rids = new int[rels.size()];
				for (int i=0; i< rels.size() ; i++) {
					rids[i]=rels.get(i).getRelId();
					log.debug("Deleting rid="+rids[i]);
				}

				try {
					proc.delete(type, owner , rids);
					log.debug("Deleted Relationships");
				} catch (PSCmsException e) {
					log.error("Cannot delete relationships",e);
				}

			}

			private void updateFields( Item item, PSCoreItem psItem) {
				if (item.getFields() != null ) {
					for (Field field : item.getFields()) {
						PSItemField psField = psItem.getFieldByName(field.getName());
						if (psField != null) {
							updateFieldValue(field,psField);
						} else {
							log.debug("Cannot find field "+field.getName()+" Ignoring");
						}
					}
				}

			}


			private void updateFieldValue(Field field, PSItemField psField) {
				psField.clearValues();
				log.debug("updating field "+field.getName());
				if (field.getValues() != null) {
					for (Value value : field.getValues()) {
						IPSFieldValue newValue = getFieldValue(value);
						psField.addValue(newValue);
					}
				} else {
					if (field.getValue() != null) {
						IPSFieldValue newValue = getFieldValue(field.getValue());
						psField.addValue(newValue);
					} else {	
						log.error("Field value is null");
					}
				}
			}


			private IPSFieldValue getFieldValue(Value value) {
				log.debug("Value type is "+value.getType());

				IPSFieldValue newValue = null;
				switch (value.getType()) { 
				// For the moment we have the text representation of the xml field not an element
				case 1: newValue = new PSTextValue(value.getStringValue()); break;
				case 2: newValue = new PSDateValue( ((DateValue)value).getDate()); break;
				default : newValue = new PSTextValue(value.getStringValue());break;
				}
				log.debug("Value type class is " + newValue.getClass() );
				return newValue;
			}

			
			
			 public String getItemXml(Item item) {
				StringWriter sw = new StringWriter();
				 try {
						
						JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
						Marshaller m = jc.createMarshaller();
						m.setProperty("jaxb.fragment", Boolean.TRUE);
						m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
						m.marshal( item, sw );
					} catch (JAXBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sw.flush();
					return sw.toString();
			 }
			 
			 public Item getItemFromStream(InputStream is) throws JAXBException {
					JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
					Unmarshaller um = jc.createUnmarshaller();
					Item item =
						  (Item)um.unmarshal( 
						    is);
					return item;
			 }
			 
			 public Document getItemDOM(Item item) {
				 DocumentResult dr = new DocumentResult();
					 try {
							
							JAXBContext jc = JAXBContext.newInstance( new Class[] {Item.class} );
							Marshaller m = jc.createMarshaller();
							m.setProperty("jaxb.fragment", Boolean.TRUE);
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
							m.marshal( item, dr );
						} catch (JAXBException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return dr.getDocument();
				 }
			 
			/**
			    * String identifying the debug assembler in Extensions.xml.
			    */
			   private static final String DEBUG_ASSEMBLER =
			         "Java/global/percussion/assembly/debugAssembler";

			   /**
			    * Used to identify error information in the bindings.
			    */
			   public static final String ERROR_VAR = "$___error___";

		}




