package org.tron.trongrid;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventLogController {

  @Autowired
  EventLogRepository eventLogRepository;
  @Autowired
  MongoTemplate mongoTemplate;


  @RequestMapping(method = RequestMethod.GET, value = "/healthcheck")
  public String  healthCheck() {
    return "OK";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/events")
  public List<ContractEvenTriggerEntity> events(
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {

    QueryFactory query = new QueryFactory();
    query.setPageniate(this.setPagniateVariable(request));
    query.setBockNum(blocknum);
    query.setTimestampGreaterEqual(timestamp);
    List<ContractEvenTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);

    return tmp;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/transaction/{transactionId}")
  public List<ContractEvenTriggerEntity> findOneByTransaction(@PathVariable String transactionId) {
    QueryFactory query = new QueryFactory();
    query.setTxid(transactionId);
    List<ContractEvenTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);
    return tmp;
  }


  @RequestMapping(method = RequestMethod.GET, value = "/event/contract/{contractAddress}")
  public List<ContractEvenTriggerEntity> findByContractAddress(@PathVariable String contractAddress,
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {
    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    query.setPageniate(this.setPagniateVariable(request));
    query.setTimestampGreaterEqual(timestamp);
    query.setBockNum(blocknum);
    System.out.println(query.toString());
    List<ContractEvenTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);
    return result;
  }

  @RequestMapping(method = RequestMethod.GET,
      value = "/event/contract/{contractAddress}/{eventName}")
  public List<ContractEvenTriggerEntity> findByContractAddressAndEntryName(
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {

    QueryFactory query = new QueryFactory();
    query.setTimestampGreaterEqual(timestamp);
    query.setBockNum(blocknum);
    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    query.setPageniate(this.setPagniateVariable(request));
    System.out.println(query.toString());

    List<ContractEvenTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);
    return result;
  }

  @RequestMapping(method = RequestMethod.GET,
      value = "/event/contract/{contractAddress}/{eventName}/{blockNumber}")
  public List<ContractEvenTriggerEntity> findByContractAddressAndEntryNameAndBlockNumber(
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @PathVariable long blockNumber) {

    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    // todo eveentname
    query.setEventName(eventName);

    query.setBockNum(blockNumber);
    List<ContractEvenTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);
    return result;

  }

  // todo eventname
  @RequestMapping(method = RequestMethod.GET,
      value = "/event/filter/contract/{contractAddress}/{eventName}")
  public List<EventLogEntity> filterevent(
      @RequestParam Map<String,String> allRequestParams,
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @RequestParam(value = "since", required = false, defaultValue = "0") Long sinceTimestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {
    Query query = new Query();
    query.addCriteria(Criteria.where("contract_address").is(contractAddress));
    query.addCriteria(Criteria.where("event_name").is(eventName));
    query.addCriteria((Criteria.where("block_timestamp").gte(sinceTimestamp)));

    if (blocknum > 0) {
      query.addCriteria((Criteria.where("blockNumber").gte(blocknum)));
    }

    try {
      JSONObject res = JSONObject.parseObject(allRequestParams.get("result"));
      for (String k : res.keySet()) {
        if (QueryFactory.isBool(res.getString(k))) {
          query.addCriteria(Criteria.where(String.format("%s.%s", "result", k))
              .is(Boolean.parseBoolean(res.getString(k))));
          continue;
        }
        query.addCriteria(Criteria.where(String.format("%s.%s", "result", k))
            .is((res.getString(k))));
      }
    } catch (JSONException e) {

    } catch (java.lang.NullPointerException e) {

    }

    query.with(this.setPagniateVariable(request));
    System.out.println(query.toString());
    List<EventLogEntity> result = mongoTemplate.find(query,EventLogEntity.class);
    return result;
  }

  // change
  @RequestMapping(method = RequestMethod.GET, value = "/event/timestamp")
  public List<ContractEvenTriggerEntity> findByBlockTimestampGreaterThan(
      @RequestParam(value = "contract", required = false) String contractAddress,
      @RequestParam(value = "since", required = false, defaultValue = "0") Long timestamp,
      HttpServletRequest request) {
    QueryFactory query = new QueryFactory();
    query.setPageniate(this.setPagniateVariable(request));
    query.setContractAddress(contractAddress);
    query.setTimestampGreaterEqual(timestamp);
    List<ContractEvenTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEvenTriggerEntity.class);

    return tmp;

  }

  private Pageable setPagniateVariable(HttpServletRequest request) {

    // variables for pagniate
    int page = 0;
    int pageSize = 20;
    String sort = "-block_timestamp";

    if (request.getParameter("page") != null && request.getParameter("page").length() > 0) {
      page = Integer.parseInt(request.getParameter("page"));
    } else {
      page = 0;
    }
    if (request.getParameter("size") != null && request.getParameter("size").length() > 0) {
      pageSize = Integer.parseInt(request.getParameter("size"));
    } else {
      pageSize = 20;
    }
    if (request.getParameter("sort") != null && request.getParameter("sort").length() > 0) {
      sort = request.getParameter("sort");
    } else {
      sort = "-block_timestamp";
    }

    return QueryFactory.make_pagination(Math.max(0,page - 1),Math.min(200,pageSize),sort);

  }

}
