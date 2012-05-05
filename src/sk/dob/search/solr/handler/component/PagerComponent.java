/** PageComponent.java
 * Program		: solr_pager
 * Version		: 0.2.0
 * Purpose		: Pager component for SOLR
 *			  Provides simple and fast paging for SOLR results
 * License		: GNU GPL v3 (see file COPYING)
 * Author		: Samuel Behan <samkob_(at)_gmail_._com> (c) 2000-2009
 * Web		: http://devel.dob.sk/solr_pager
 * Requirements	: JAVA 1.5, SOLR 1.4, ANT
 */
package sk.dob.search.solr.handler.component;

/* solr imports */
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;


/* lf4j import */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* java imports */
import java.io.IOException;
import java.net.URL;


/**
 * PagerComponent
 *   Provides simple and fast paging for SOLR results
 *
 * @version 0.2.0 SamB $
 * @since solr 1.4
 */
public class PagerComponent extends SearchComponent 
{
  public static final String COMPONENT_NAME = "pager";
  public static final String PARAM_PAGER = "pager";
  public static final String PARAM_PAGER_PRE = "pager.pre";
//  private static final Logger log = LoggerFactory.getLogger(PagerComponent.class);

  @Override
  public void prepare(ResponseBuilder rb) throws IOException
  {
	/* notify solr we always need resultset ! */
	if(rb.req.getParams().getInt(PARAM_PAGER, 0) != 0)
		rb.setNeedDocSet( true );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void process(ResponseBuilder rb) throws IOException 
  {
	/* get request params */
	SolrParams par = rb.req.getParams();
	int rows = par.getInt(CommonParams.ROWS, 0);
	int start = par.getInt(CommonParams.START, 0);
	int pages = par.getInt(PARAM_PAGER, 0);
	int pages_pre = par.getInt(PARAM_PAGER_PRE, 2);

	/* neet to work ? */
	if(pages == 0 || rows == 0 || rb == null 
		|| rb.getResults() == null)
		return;

	/* select result list */
	int doc_count = 0; 

	if(rb.getResults().docSet != null)
		doc_count = rb.getResults().docSet.size();
	else return;

	/* pager list */
	NamedList lst = new SimpleOrderedMap<Object>();
	NamedList lst2 = new SimpleOrderedMap<Object>();

	/* paging pages */
	int page_count = doc_count / rows;
	int page_actual = start / rows;
	int page_pre = pages_pre;
	int page_post = pages - page_pre - 1;

	/* last page */
	if(doc_count % rows != 0)
		page_count++;

	/* page range */
	if(page_actual - page_pre < 0)
	{
		page_post += - (page_actual - page_pre);
		page_pre  -= - (page_actual - page_pre);
	}
	else if(page_actual + page_post > page_count)
	{
		page_post = pages - page_pre;
		page_pre  = page_actual + pages - page_count;
	}

	/* sanity */
	if(page_pre < 0)
		page_pre = 0;
	if(page_post < 0)
		page_post = 0;

	/* next pages list */
	int i = (page_actual - page_pre);
	for(i = (i <= 0 ? 0 : i);
		i < page_count && i <= (page_actual + page_post);
		i++)
		lst2.add(Integer.toString(i + 1), i * rows);
	lst.add("pages", lst2);

	/* navi */
	if(page_actual > 0)
		lst.add("prev", (page_actual - 1) * rows);
	if(page_actual - page_pre > 0)
		lst.add("first", 0);
	if(page_actual < (page_count - 1))
		lst.add("next", (page_actual + 1) * rows);
	if(page_actual + page_post < (page_count - 1))
		lst.add("last", (page_count - 1) * rows);
	lst.add("actual", page_actual + 1);
	lst.add("count", page_count);

	/* finish */
	rb.rsp.add("pager", lst);
  }

  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) 
  {
  }

  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) 
  {
  }

  @Override
  public void finishStage(ResponseBuilder rb) 
  {
  }

  ////////////////////////////////////////////
  ///  SolrInfoMBean
  ////////////////////////////////////////////
  
  @Override
  public String getDescription() {
    return "Pager";
  }
  
  @Override
  public String getVersion() {
    return "$Revision: 1 $";
  }
  
  @Override
  public String getSourceId() {
    return "$Id: $";
  }
  
  @Override
  public String getSource() {
    return "$URL: http://devel.dob.sk/solr_pager $";
  }
  
  @Override
  public URL[] getDocs() {
    return null;
  }
}
