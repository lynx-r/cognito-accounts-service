package com.workingbit.accounts.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.workingbit.accounts.config.AwsProperties;
import com.workingbit.accounts.exception.DataAccessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 20:11 10/06/2017.
 */
@Service
public class DynamoDbService {


  private final AmazonDynamoDB ddb;
  private final AwsProperties awsProperties;

  /**
   * Looks up table name and creates one if it does not exist
   */
  @Autowired
  public DynamoDbService(AwsProperties awsProperties) {
    this.awsProperties = awsProperties;
    ddb = AmazonDynamoDBClientBuilder.defaultClient();

    try {
      if (!doesTableExist(awsProperties.getUserTable())) {
        createDeviceTable();
      }
    } catch (DataAccessException e) {
      throw new RuntimeException("Failed to create device table.", e);
    }
  }

  /**
   * Store the username, password combination in the Identity table. The
   * username will represent the item name and the item will contain a
   * attributes password and userid.
   * @param username
 *            Unique user identifier
   * @param email
   * @param identityId
*          got from getId method
   */
  public void storeIdentityId(String username, String email, String identityId) throws DataAccessException {
    if (null == identityId) {
      return;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeUid(), new AttributeValue().withS(username));
    item.put(awsProperties.getAttributeUsername(), new AttributeValue().withS(username));
    item.put(awsProperties.getAttributeEmail(), new AttributeValue().withS(email));
    item.put(awsProperties.getAttributeIdentityId(), new AttributeValue().withS(identityId));
    item.put(awsProperties.getAttributeEnabled(), new AttributeValue().withS("true"));

    PutItemRequest putItemRequest = new PutItemRequest()
        .withTableName(awsProperties.getUserTable())
        .withItem(item);
    try {
      ddb.putItem(putItemRequest);
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to store user: " + username, e);
    }
  }

  public void storeUser(String username, String password) throws DataAccessException {
    if (StringUtils.isBlank(username)) {
      return;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeUid(), new AttributeValue().withS(username));
    item.put(awsProperties.getAttributePassword(), new AttributeValue().withS(password));
    item.put(awsProperties.getAttributeEnabled(), new AttributeValue().withS("true"));

    PutItemRequest putItemRequest = new PutItemRequest()
        .withTableName(awsProperties.getUserTable())
        .withItem(item);
    try {
      ddb.putItem(putItemRequest);
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to store user: " + username, e);
    }
  }

  public Map<String, AttributeValue> retrieveByUsername(String username) throws DataAccessException {
    if (StringUtils.isBlank(username)) {
      return null;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeUid(), new AttributeValue().withS(username));

    GetItemRequest getItemRequest = new GetItemRequest()
        .withTableName(awsProperties.getUserTable())
        .withKey(item);
    try {
      GetItemResult result = ddb.getItem(getItemRequest);
      return result.getItem();
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to store user: " + username, e);
    }
  }

  public String retrieveByIdentityId(String identityId) throws DataAccessException {
    if (StringUtils.isNotBlank(identityId)) {
      return null;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeIdentityId(), new AttributeValue().withS(identityId));

    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":identityId", new AttributeValue(identityId));
    ScanRequest getItemRequest = new ScanRequest()
        .withTableName(awsProperties.getUserTable())
        .withFilterExpression("identity_id = :identityId")
        .withExpressionAttributeValues(expressionAttributeValues);
    try {
      ScanResult result = ddb.scan(getItemRequest);
      return result.getItems().get(0).get(awsProperties.getAttributeUid()).getS();
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to retrieve identityId: " + identityId, e);
    }
  }

  /**
   * Checks to see if given tableName exist
   *
   * @param tableName
   *            The table name to check
   * @return true if tableName exist, false otherwise
   */
  protected boolean doesTableExist(String tableName) throws DataAccessException {
    try {
      DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
      DescribeTableResult result = ddb.describeTable(request);
      return "ACTIVE".equals(result.getTable().getTableStatus());
    } catch (ResourceNotFoundException e) {
      return false;
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to get status of table: " + tableName, e);
    }
  }

  /**
   * Used to create the device table. This function only needs to be called
   * once.
   */
  protected void createDeviceTable() throws DataAccessException {
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
        .withReadCapacityUnits(awsProperties.getReadCapacityUnits())
        .withWriteCapacityUnits(awsProperties.getWriteCapacityUnits());

    ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(new AttributeDefinition().withAttributeName(
        awsProperties.getAttributeUid()).withAttributeType("S"));

    ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
    tableKeySchema.add(new KeySchemaElement().withAttributeName(awsProperties.getAttributeUid())
        .withKeyType(KeyType.HASH));

    CreateTableRequest createTableRequest = new CreateTableRequest()
        .withTableName(awsProperties.getUserTable())
        .withProvisionedThroughput(provisionedThroughput)
        .withAttributeDefinitions(attributeDefinitions)
        .withKeySchema(tableKeySchema);

    try {
      ddb.createTable(createTableRequest);
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to create table: " + awsProperties.getUserTable(), e);
    }
  }
}
