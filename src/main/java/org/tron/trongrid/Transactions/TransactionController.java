package org.tron.trongrid.Transactions;

import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.tron.trongrid.BlockTriggerEntity;
import org.tron.trongrid.QueryFactory;
import org.tron.trongrid.TransactionTriggerEntity;

@RestController
@Component
@PropertySource("classpath:tronscan.properties")
public class TransactionController {
    @Value("${url.transaction}")
    private String url;

    @RequestMapping(method = RequestMethod.GET, value = "/totaltransactions")
    public Long totaltransaction() {

        JSONObject result = this.getResponse(this.url);
        return result.getLong("total");
    }
    @Autowired(required = false)
    MongoTemplate mongoTemplate;

    @RequestMapping(method = RequestMethod.GET, value = "/transactions")
    public JSONObject getTranssactions(
      /******************* Page Parameters ****************************************************/
      @RequestParam(value="limit", required=false, defaultValue = "40" ) int limit,
      @RequestParam(value="count", required=false, defaultValue = "true" ) boolean count,
      @RequestParam(value="sort", required=false, defaultValue = "-timestamp") String sort,
      @RequestParam(value="start", required=false, defaultValue = "0") Long start,
      @RequestParam(value="total", required=false, defaultValue = "0") Long total,
      /****************** Filter parameters *****************************************************/
      @RequestParam(value="block", required=false, defaultValue = "-1") long block

    ){

        QueryFactory query = new QueryFactory();
        if(block > 0) {
            query.setBockNum(block);
        }
        query.setPageniate(this.setPagniateVariable(start, limit, sort));
        List<TransactionTriggerEntity> tmp = mongoTemplate.find(query.getQuery(), TransactionTriggerEntity.class);

        Map map = new HashMap();

        map.put("total", tmp.size());
        map.put("data", tmp);
        return new JSONObject(map);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/transactions/{hash}")
    public JSONObject getTransactionbyHash(
      @PathVariable String hash
    ){

        QueryFactory query = new QueryFactory();
        query.setHashEqual(hash);
        List<TransactionTriggerEntity> tmp = mongoTemplate.find(query.getQuery(), TransactionTriggerEntity.class);
        if (tmp.size() == 0) return null;
        Map map = new HashMap();
        map.put("transaction", tmp.get(0));

        return new JSONObject(map);
    }

    private JSONObject getResponse(String url){
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();
        return JSON.parseObject(restTemplate.getForObject(url, String.class));
    }

    private Pageable setPagniateVariable(long start, int size, String sort){
        int page = Math.max(0,(int)start / size);
        int page_size = size;
        return QueryFactory.make_pagination(Math.max(0,page-1),Math.min(200,page_size),sort);
    }

}
