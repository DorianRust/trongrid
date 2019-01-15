package org.tron.trongrid.transfers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.tron.trongrid.ContractEventTriggerEntity;
import org.tron.trongrid.QueryFactory;


@RestController
@Component
@PropertySource("classpath:tronscan.properties")
public class TransferController {

  @Value("${url.transfers}")
  private String url;

  @Autowired(required = false)
  MongoTemplate mongoTemplate;

  @RequestMapping(method = RequestMethod.GET, value = "/totaltransfers")
  public Long totaltransfers() {
    QueryFactory query = new QueryFactory();
    query.findAllTransfer("Transfer");
    return new Long(mongoTemplate.count(query.getQuery(), ContractEventTriggerEntity.class));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/totaltransfers/{address}")
  public Long addressTotaltransfers(
      @PathVariable String address
  ) {
    QueryFactory query = new QueryFactory();
    query.findAllTransferByAddress(address);
    return mongoTemplate.count(query.getQuery(), ContractEventTriggerEntity.class);
  }


  @RequestMapping(method = RequestMethod.GET, value = "/transfers")
  public JSONArray getTransfers(
      /******************* Page Parameters ****************************************************/
      @RequestParam(value = "limit", required = false, defaultValue = "40") int limit,
      @RequestParam(value = "count", required = false, defaultValue = "true") boolean count,
      @RequestParam(value = "sort", required = false, defaultValue = "-timeStamp") String sort,
      @RequestParam(value = "start", required = false, defaultValue = "0") Long start,
      @RequestParam(value = "total", required = false, defaultValue = "0") Long total,
      /****************** Filter parameters *****************************************************/
      @RequestParam(value = "address", required = false, defaultValue = "") String address,
      @RequestParam(value = "from", required = false, defaultValue = "") String from,
      @RequestParam(value = "to", required = false, defaultValue = "") String to,
      @RequestParam(value = "token", required = false, defaultValue = "") String token

  ) {

    String url = String.format("%s?limit=%d&sort=%s&count=%b&start=%d&total=%d",
        this.url,limit,sort,count,start,total);
    if (address != null && address.length() > 0) {
      url = String.format("%s&address=%s", url, address);
    }
    if (from != null && from.length() > 0) {
      url = String.format("%s&from=%s", url, from);
    }
    if (to != null && to.length() > 0) {
      url = String.format("%s&to=%s", url, to);
    }
    if (token != null && token.length() > 0) {
      url = String.format("%s&token=%s", url, token);
    }

    JSONObject result = this.getResponse(url);
    return result.getJSONArray("data");
  }

  @RequestMapping(method = RequestMethod.GET, value = "/transfers/{hash}")
  public JSONObject getTrnasferbyHash(
      @PathVariable String hash
  ) {
    QueryFactory query = new QueryFactory();
    query.findAllTransferByTransactionId(hash);

    List<ContractEventTriggerEntity> result =  mongoTemplate.find(query.getQuery(),
        ContractEventTriggerEntity.class);
    if (result.size() == 0) {
      return null;
    }

    Map map = new HashMap();
    map.put("data", result.get(0));
    return new JSONObject(map);
  }

  private JSONObject getResponse(String url) {
    System.out.println(url);
    RestTemplate restTemplate = new RestTemplate();
    return JSON.parseObject(restTemplate.getForObject(url, String.class));
  }


}
