package org.tron.trongrid;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
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

  @RequestMapping(method = RequestMethod.GET, value = "/events")
  public List<ContractEventTriggerEntity> events(
      @RequestParam(value = "since", required = false, defaultValue = "0") long timestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      HttpServletRequest request) {

    QueryFactory query = new QueryFactory();
    query.setPageniate(this.setPagniateVariable(request));
    if (blocknum != -1) {
      query.setBlockNumGte(blocknum);
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

  // get event list
  @RequestMapping(method = RequestMethod.GET, value = "/events/{contractAddress}")
  public List<JSONObject> findEventsListByContractAddress
      (@PathVariable String contractAddress,
       @RequestParam(value = "limit", required = false, defaultValue = "25") int limit,
       @RequestParam(value = "sort", required = false, defaultValue = "-timeStamp") String sort,
       @RequestParam(value = "start", required = false, defaultValue = "0") int start
      ) {

    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    query.setPageniate(this.setPagniateVariable(limit, sort, start));
    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);

    List<JSONObject> array = new ArrayList<>();
    for(ContractEventTriggerEntity p : result) {
      Map map = new HashMap();
      map.put("TxHash", p.getTransactionId());
      map.put("BlockNum", p.getBlockNumer());
      map.put("eventTime", p.getTimeStamp());
      map.put("eventFunction", p.getEventSignature());
      int i = 0;
      Map<String, String> dataMap = p.getDataMap();
      Map<String, String> topicMap = p.getTopicMap();
      for (String topic : topicMap.keySet()) {
        dataMap.put(topic, topicMap.get(topic));
      }

      while (dataMap.containsKey(String.valueOf(i))) {
        map.put(String.valueOf(i), dataMap.get(String.valueOf(i)));
        i++;
      }
      array.add(new JSONObject(map));
    }

    return array;
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
    query.setEventName(eventName);

    if (blockNumber != -1) {
      query.setBlockNumGte(blockNumber);
    }

    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    return result;

  }

  @RequestMapping(method = RequestMethod.GET,
      value = "/event/filter/contract/{contractAddress}/{eventName}")
  public List<ContractEventTriggerEntity> filterevent(
      @RequestParam Map<String,String> allRequestParams,
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @RequestParam(value = "since", required = false, defaultValue = "0") Long sinceTimestamp,
      @RequestParam(value = "block", required = false, defaultValue = "-1") long blocknum,
      @RequestParam(value = "limit", required = false, defaultValue = "25") int limit,
      @RequestParam(value = "sort", required = false, defaultValue = "-timeStamp") String sort,
      @RequestParam(value = "start", required = false, defaultValue = "0") int start) {
    QueryFactory query = new QueryFactory();

    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    query.setTimestampGreaterEqual(sinceTimestamp);

    query.setPageniate(this.setPagniateVariable(limit, sort, start));
    System.out.println(query.toString());
    List<ContractEventTriggerEntity> result = mongoTemplate.find(query.getQuery(), ContractEventTriggerEntity.class);
    return result;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/timestamp")
  public List<ContractEventTriggerEntity> findByBlockTimestampGreaterThan(
      @RequestParam(value = "contract", required = false) String contractAddress,
      @RequestParam(value = "since", required = false, defaultValue = "0") Long timestamp,
      HttpServletRequest request) {
    QueryFactory query = new QueryFactory();
    query.setContractAddress(contractAddress);
    query.setTimestampGreaterEqual(timestamp);
    query.setPageniate(this.setPagniateVariable(request));
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

  private Pageable setPagniateVariable(int limit, String sort, int start) {

    // variables for pagniate
    int page = start;
    int pageSize = limit;

    return QueryFactory.make_pagination(Math.max(0,page - 1),Math.min(200,pageSize),sort);

  }

}
