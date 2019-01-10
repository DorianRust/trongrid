package org.tron.trongrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "transaction")
public class TransactionTriggerEntity implements Serializable {

  private static final long serialVersionUID = -70777625567836430L;

  @Id
  private String id;

  @Field(value = "transactionId")
  @JsonProperty(value = "transactionId")
  private String transactionId;

  @Field(value = "blockId")
  @JsonProperty(value = "blockId")
  private String blockId;

  @Field(value = "blockNum")
  @JsonProperty(value = "blockNum")
  private long blockNum;

  @Field(value="energyUsage")
  @JsonProperty(value="energyUsage")
  private long energyUsage;

  @Field(value = "energyFee")
  @JsonProperty(value = "energyFee")
  private Long energyFee;

  @Field(value = "originEnergyUsage")
  @JsonProperty(value = "originEnergyUsage")
  private Long originEnergyUsage;

  @Field(value = "energyUsageTotal")
  @JsonProperty(value = "energyUsageTotal")
  private Long energyUsageTotal;

  @Field(value = "netUsage")
  @JsonProperty(value = "netUsage")
  private Long netUsage;

  @Field(value = "netFee")
  @JsonProperty(value = "netFee")
  private Long netFee;

  @Field(value = "timeStamp")
  @JsonProperty(value = "timeStamp")
  private Long timeStamp;

  @Field(value = "triggerName")
  @JsonProperty(value = "triggerName")
  private String triggerName;

  public TransactionTriggerEntity(String transactionId, String blockId,  long blockNum, long energyUsage,
    long energyFee, long originEnergyUsage, long energyUsageTotal, long netUsage, long netFee) {
    this.transactionId = transactionId;
    this.blockId = blockId;
    this.blockNum = blockNum;
    this.energyUsage = energyUsage;
    this.energyFee = energyFee;
    this.originEnergyUsage = originEnergyUsage;
    this.energyUsageTotal = energyUsageTotal;
    this.netUsage = netUsage;
    this.energyUsage = energyUsage;
    this.netFee = netFee;
  }
}