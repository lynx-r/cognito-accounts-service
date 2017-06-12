package online.shashki.accounts.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import online.shashki.accounts.config.AwsProperties;
import online.shashki.accounts.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
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
   * @param identityId
   *          got from getId method
   * @param email
 *            Unique user identifier
   */
  public void storeIdentityId(String email, String identityId) throws DataAccessException {
    if (null == identityId) {
      return;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeUid(), new AttributeValue().withS(email));
    item.put(awsProperties.getAttributeUsername(), new AttributeValue().withS(email));
    item.put(awsProperties.getAttributeEmail(), new AttributeValue().withS(email));
    item.put(awsProperties.getAttributeIdentityId(), new AttributeValue().withS(identityId));
    item.put(awsProperties.getAttributeEnabled(), new AttributeValue().withS("true"));

    PutItemRequest putItemRequest = new PutItemRequest()
        .withTableName(awsProperties.getUserTable())
        .withItem(item);
    try {
      ddb.putItem(putItemRequest);
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to store user: " + email, e);
    }
  }

  public String retrieveIdentityId(String email) throws DataAccessException {
    if (null == email) {
      return null;
    }

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(awsProperties.getAttributeUid(), new AttributeValue().withS(email));

    GetItemRequest getItemRequest = new GetItemRequest()
        .withTableName(awsProperties.getUserTable())
        .withKey(item)
        .withAttributesToGet(awsProperties.getAttributeIdentityId());
    try {
      GetItemResult result = ddb.getItem(getItemRequest);
      return result.getItem().get(awsProperties.getAttributeIdentityId()).getS();
    } catch (AmazonClientException e) {
      throw new DataAccessException("Failed to store user: " + email, e);
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
