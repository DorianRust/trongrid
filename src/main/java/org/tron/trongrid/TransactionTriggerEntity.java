package org.tron.trongrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
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

  @Field(value = "blockHash")
  @JsonProperty(value = "blockHash")
  private String blockHash;

  @Field(value = "blockNumber")
  @JsonProperty(value = "blockNumber")
  private long blockNumber;

  @Field(value = "energyUsage")
  @JsonProperty(value = "energyUsage")
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

  @Field(value = "internalTrananctionList")
  @JsonProperty(value = "internalTrananctionList")
  private List<InternalTransactionPojo> internalTrananctionList;

  @Field(value = "fromAddress")
  @JsonProperty(value = "fromAddress")
  private String fromAddress;

  @Field(value = "toAddress")
  @JsonProperty(value = "toAddress")
  private String toAddress;

  @Field(value = "assetName")
  @JsonProperty(value = "assetName")
  private String assetName;

  @Field(value = "assetAmount")
  @JsonProperty(value = "assetAmount")
  private long assetAmount;

  public TransactionTriggerEntity(String transactionId, String blockHash,
      long blockNumber, long energyUsage, long energyFee, long originEnergyUsage,
      long energyUsageTotal, long netUsage, long netFee, List<InternalTransactionPojo> internalTrananctionList,
      String fromAddress, String toAddress, String assetName, long assetAmount) {
    this.transactionId = transactionId;
    this.blockHash = blockHash;
    this.blockNumber = blockNumber;
    this.energyUsage = energyUsage;
    this.energyFee = energyFee;
    this.originEnergyUsage = originEnergyUsage;
    this.energyUsageTotal = energyUsageTotal;
    this.netUsage = netUsage;
    this.energyUsage = energyUsage;
    this.netFee = netFee;
    this.internalTrananctionList = internalTrananctionList;
    this.fromAddress = fromAddress;
    this.toAddress = toAddress;
    this.assetName = assetName;
    this.assetAmount = assetAmount;
  }
}