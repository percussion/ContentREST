<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
	import="javax.jcr.query.Query"
    import="javax.jcr.query.QueryResult"  
    import="javax.jcr.query.RowIterator" 
    import="javax.jcr.query.Row" 
    import="javax.jcr.Value" 
    import="javax.jcr.Node"
    import="java.util.Map, java.util.Set, java.util.Collections, java.util.Map.Entry, java.util.Iterator, java.util.HashMap, java.util.Arrays, java.util.ArrayList, java.util.List, org.apache.commons.lang.StringUtils, javax.servlet.jsp.JspWriter"
    import="com.percussion.services.contentmgr.IPSContentMgr"
    import="com.percussion.services.contentmgr.PSContentMgrLocator"
    import="com.percussion.webservices.content.PSContentWsLocator"
    import="com.percussion.webservices.content.IPSContentWs"
    import="com.percussion.utils.guid.IPSGuid"
    import="com.percussion.server.webservices.PSServerFolderProcessor"
    import="com.percussion.server.PSRequest"
    import="com.percussion.utils.request.PSRequestInfo"
    import="com.percussion.design.objectstore.PSLocator"
    import="com.percussion.cms.objectstore.PSComponentSummary"
    import="com.percussion.webservices.security.IPSSecurityWs"
    import="com.percussion.webservices.security.PSSecurityWsLocator"
    import="com.percussion.services.guidmgr.data.PSLegacyGuid"
    import="com.percussion.services.legacy.IPSCmsObjectMgr"
    import="com.percussion.services.legacy.PSCmsObjectMgrLocator"
    import="com.percussion.cms.PSCmsException"
    import="com.percussion.webservices.PSErrorResultsException"
    import="com.percussion.services.security.data.PSCommunity"
    import="com.percussion.cms.objectstore.PSObjectAclEntry"
    import="com.percussion.cms.objectstore.IPSDbComponent"
    import="com.percussion.cms.objectstore.PSObjectAcl"
    import="com.percussion.cms.objectstore.PSFolder"
	import="com.percussion.services.PSBaseServiceLocator"
	import="com.percussion.services.guidmgr.PSGuidManagerLocator"
	import="com.percussion.pso.utils.PSOItemSummaryFinder"
	import="com.percussion.util.IPSHtmlParameters"
	import="com.percussion.services.contentmgr.IPSContentMgr"
	import="com.percussion.services.guidmgr.IPSGuidManager"
	import="com.percussion.pso.utils.IPSOItemSummaryFinder"
	import="com.percussion.services.contentmgr.IPSNode"
	import="com.percussion.services.guidmgr.PSGuidUtils"
	import="com.percussion.services.contentmgr.PSContentMgrConfig"
%>

	<%!
	//initialize variables used in the JSP page
	
	IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
	IPSContentMgr mgr = PSContentMgrLocator.getContentMgr(); 
	IPSContentWs contentWs = PSContentWsLocator.getContentWebservice(); 
	IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
	IPSSecurityWs securityWs = PSSecurityWsLocator.getSecurityWebservice();
	IPSOItemSummaryFinder isFinder = null; 
	List myGuids;
	PSContentMgrConfig myConfig = null;
	
	%>
	
	<%
	String cid=request.getParameter("sys_contentid");
	PSLocator loc = isFinder.getCurrentOrEditLocator(cid);
	IPSGuid contentGUID=gmgr.makeGuid(loc);
	List myGuid = PSGuidUtils.toGuidList(contentGUID);
	List contentList = mgr.findItemsByGUID(myGuid, myConfig);

	IPSNode contentNode = (IPSNode)contentList.get(0);
	String cmsURL = contentNode.getProperty("feedFormat").toString();
	String feedURL = contentNode.getProperty("feedUrl").toString();
	%>

<html><head><title>REST Import Service</title>
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js"></script>
	<script type="text/javascript">
	function htmlEncode(value) {
	    return $('<div/>').text(value).html();
	} 
	  $(function(){
	    $("#form").submit(function(){
	        dataString = request.getParameter("");
			posturl = $("#post_url").val();
			
	        $.ajax({
	        type: "POST",
	        // url: "/Rhythmyx/services/Content/import/cdcSyndicationImport",
			url: posturl,
	        data: dataString,
			contentType: "text/xml",
	        dataType: "xml",
	        complete: function(xhr, status) {
	 	//	alert(data);
		
	            $("#message_ajax").html(htmlEncode(xhr.responseText));
	        }
	 
	        });
	 
	        return false;           
	 
	    });
	});
	</script>
  </head>
<body>

<table width="100%" height="66" background="/Rhythmyx/sys_resources/images/banner_bkgd.jpg" style="background-attachment: fixed; background-repeat: no-repeat;">
<tbody><tr>
<td>
</td>
</tr>
</tbody></table>
<h1>REST Import Service</h1>
	<form id="form" action="/Rhythmyx/services/Content/import/cvaSyndicationImport" method="post">
		<!--  CMS IMPORT URL:<br/><textarea rows="2" cols="80" NAME="post_url" id="post_url" ></textarea><br>  -->
		FEED URL:<br/>
		<textarea rows="2" cols="80" name="body" id="body"></textarea><br/>
		<input type="submit"></input>
	</form>
</body>
</html>