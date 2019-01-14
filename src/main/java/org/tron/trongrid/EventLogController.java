package org.tron.trongrid;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  @RequestMapping(method = RequestMethod.GET, value = "/events/topics/{topics}")
  public List<ContractEventTriggerEntity> findByContractTopic(
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "topics", required = false, defaultValue = "") String topics
  ) {
    QueryFactory query = new QueryFactory();
    query.likeTopicMap("2");
    List<ContractEventTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return tmp;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/events")
  public List<ContractEventTriggerEntity> events(
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {

    QueryFactory query = new QueryFactory();
    query.setPageniate(this.setPagniateVariable(request));
    if (blocknum != -1) {
      query.setBlockNum(blocknum);
    }
    query.setTimestampGreaterEqual(timestamp);
    List<ContractEventTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);

    return tmp;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/transaction/{transactionId}")
  public List<ContractEventTriggerEntity> findOneByTransaction(@PathVariable String transactionId) {
    QueryFactory query = new QueryFactory();
    query.setTransactionIdEqual(transactionId);
    List<ContractEventTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return tmp;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/contractAddress/{contractAddress}")
  public List<ContractEventTriggerEntity> findByContractAddress(@PathVariable String contractAddress,
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {
    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    query.setPageniate(this.setPagniateVariable(request));
    query.setTimestampGreaterEqual(timestamp);
    if (blocknum != -1) {
      query.setBlockNum(blocknum);
    }
    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return result;
  }

  @RequestMapping(method = RequestMethod.GET,
      value = "/event/contract/{contractAddress}/{eventName}")
  public List<ContractEventTriggerEntity> findByContractAddressAndEntryName(
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {

    QueryFactory query = new QueryFactory();
    query.setTimestampGreaterEqual(timestamp);
    if (blocknum != -1) {
      query.setBlockNum(blocknum);
    }
    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    query.setPageniate(this.setPagniateVariable(request));
    System.out.println(query.toString());

    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return result;
  }

  @RequestMapping(method = RequestMethod.GET,
      value = "/event/contract/{contractAddress}/{eventName}/{blockNumber}")
  public List<ContractEventTriggerEntity> findByContractAddressAndEntryNameAndBlockNumber(
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @PathVariable long blockNumber) {

    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    // todo eveentname
    query.setEventName(eventName);

    if (blockNumber != -1) {
      query.setBlockNum(blockNumber);
    }

    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
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

  @RequestMapping(method = RequestMethod.GET, value = "/event/timestamp")
  public List<ContractEventTriggerEntity> findByBlockTimestampGreaterThan(
      @RequestParam(value = "contract", required = false) String contractAddress,
      @RequestParam(value = "since", required = false, defaultValue = "0") Long timestamp,
      HttpServletRequest request) {
    QueryFactory query = new QueryFactory();
    query.setPageniate(this.setPagniateVariable(request));
    query.setContractAddress(contractAddress);
    query.setTimestampGreaterEqual(timestamp);
    List<ContractEventTriggerEntity> tmp = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return tmp;
  }


  @RequestMapping(method = RequestMethod.GET, value = "/trc20/getholder/{contractAddress}")
  public  List<String> totalholder(
      @PathVariable String contractAddress
  ) {
    QueryFactory query = new QueryFactory();
    query.findAllTransferByAddress(contractAddress);

    List<ContractEventTriggerEntity> contractList = mongoTemplate.find(query.getQuery(), ContractEventTriggerEntity.class);
    Set<String> addressSet = new HashSet<>();
    for (ContractEventTriggerEntity contract : contractList) {
      Map<String, String> topMap = contract.getTopicMap();
      if (topMap.containsKey("_to") && topMap.containsKey("_from")) {
        addressSet.add(topMap.get("_to"));
        addressSet.add(topMap.get("_from"));
      }
    }

    return  addressSet.stream().collect(Collectors.toList());
  }

  // only use to test
  @RequestMapping(method = RequestMethod.GET, value = "/trc20/allAddressHolderNum")
  public  JSONObject allHolderNumber(
  ) {
    QueryFactory query = new QueryFactory();
    query.findAllTransfer("Transfer");

    List<ContractEventTriggerEntity> contractList = mongoTemplate.find(query.getQuery(), ContractEventTriggerEntity.class);
    Map<String, Set<String>> addressSet = new HashMap<String, Set<String>>();
    for (ContractEventTriggerEntity contract : contractList) {
      String contractAddr = contract.getContractAddress();
      Map<String, String> topMap = contract.getTopicMap();
      if (topMap.containsKey("_to") && topMap.containsKey("_from")) {
        if (addressSet.containsKey(contractAddr)) {
          addressSet.get(contractAddr).add(topMap.get("_to"));
          addressSet.get(contractAddr).add(topMap.get("_from"));
        } else {
          Set<String> insert = new HashSet<String>();
          insert.add(topMap.get("_to"));
          insert.add(topMap.get("_from"));
          addressSet.put(contractAddr,insert );
        }
      }
    }

    Map map = new HashMap();
    for (String address : addressSet.keySet()) {
      map.put(address, addressSet.get(address).size());
    }
    return new JSONObject(map);
  }

  private Pageable setPagniateVariable(HttpServletRequest request) {

    // variables for pagniate
    int page = 0;
    int pageSize = 20;
    String sort = "-timeStamp";

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
      sort = "-timeStamp";
    }

    return QueryFactory.make_pagination(Math.max(0,page - 1),Math.min(200,pageSize),sort);

  }

}
