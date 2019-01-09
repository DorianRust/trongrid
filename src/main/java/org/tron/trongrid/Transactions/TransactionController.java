package org.tron.trongrid.Transactions;

import com.alibaba.fastjson.JSONObject;
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
    public List<BlockTriggerEntity> getTranssactions(
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
        List<BlockTriggerEntity> tmp = mongoTemplate.find(query.getQuery(), BlockTriggerEntity.class);

        return tmp;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/transactions/{hash}")
    public JSONObject getTransactionbyHash(
      @PathVariable String hash
    ){
        String url = String.format("%s/%s",this.url,hash);
        JSONObject result = this.getResponse(url);
        return result;
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
