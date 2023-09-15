# we-sdk-spring

Java/Kotlin SDK starter with autoconfigurations for node and contract clients.
Also contains autoconfigurations for signing SignRequests via tx-signer and logic for building atomics.

## General
To start using the library, you need to add a dependency on starters needed for your project.  
List of possible starters:
* [we-starter-atomic](#we-starter-atomic)
* [we-starter-contract-client](#we-starter-contract-client)
* [we-starter-node-client](#we-starter-node-client)
* [we-starter-tx-signer](#we-starter-tx-signer)

## we-starter-node-client
A starter that provides separate services for interacting with the node.  
List of services: `TxService`, `ContractService`, `AddressService`, `NodeInfoService`, `PrivacyService`, `BlocksService`, `BlockchainEventsService`, `UtilsService`. 
The names of the services match the API parts of the WE Node.
The main autoconfigurations of this starter are:
* [NodeBlockingServiceFactoryAutoConfiguration](com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration) 
which builds basic `NodeBlockingServiceFactory` to be wrapped further.
* [NodeServicesAutoConfiguration](we-autoconfigure/src/main/kotlin/com/wavesenterprise/sdk/spring/autoconfigure/node/service/NodeServicesAutoConfiguration.kt) 
 which invokes basic `NodeServicesAutoConfiguration` wrapped by all additional post processors to create services being used in the client code.  

_For more information on wrapping the behaviour of service methods invoking WE Node and we-node-client interfaces, see the [we-node-client documentation](https://github.com/waves-enterprise/we-node-client)._  
### Adding the dependency
Gradle:
```
implementation("com.wavesenterprise:we-starter-node-client")
```
Maven:
```xml
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-node-client</artifactId>
    <version>${version}</version>
</dependency>
```
### Configuration
Before you start working with `we-starter-node-client`, you need to set values in the configuration properties for the WE Nodes being used.  
Configuration properties are described in the [NodeProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FNodeProperties.kt).

_**Note:** When switching from vst-commons legacy library client, you need to add `node.legacy-mode: true`_
#### Example of configuration for a single node
```yaml
node:
  config:
    node-0:                                 # node alias
      http:                                 # http connection settings
        url: http://localhost:8080/node-0/  # url of the node
        xApiKey: key                        # key for X-Api-Key header when working with secure endpoints
        xPrivacyApiKey: key                 # key for X-Api-Key header when working with privacy endpoints
        feign:                              # Feign client specific settings
          decode404: true
          connectTimeout: 5000
          readTimeout: 5000
          loggerLevel: FULL
      grpc:                                 # GRPC connection settings
        address: localhost                  # node grpc address 
        port: 6865                          # node grpc port
  credentials-provider:                     # credentials for singing transactions with addresses from the WE Node KeyStore
    addresses:
      address: password                     # {address}: {password}
```
If necessary, you can configure the following provided wrappers for NodeBlockingServiceFactory:
* **RateLimitingServiceFactory** - configured in [RateLimiterProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FRateLimiterProperties.kt);
```yaml
node:
  rate-limiter:         # example with default values
    enabled: true   
    maxUtx: 50          # value for limit requests if UTX pool is overflown
    minWaits: 1s        # minimum waiting time before retrying request to the Node
    maxWait: 3s         # maximum waiting time before retrying request to the node
    maxWaitTotal: 10s   # maximum wait time before throwing TooManyRequests exception
```
* **CachingNodeBlockingServiceFactory** - configured in [CacheProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FCacheProperties.kt). 
```yaml
node:
  cache:                          # example with default values
    enabled: true
    txCacheSize: 5000             # maximum number of transactions allowed to be stored in cache
    policyItemInfoCacheSize: 500  # maximum number of policy item infos allowed to be stored in cache
    cacheDuration: 500s           # time limit on cache storage
```
* **LoadBalancingServiceFactory** - Does not have separate settings. Works when clients to several nodes are configured in the Node config;

### Wrapping Node Services
The starter design allows you to add client wrappers in client code via post processors and use node services with additional logic.

You can find an example of implemented wrapper in [AtomicAwareNodeBlockingServiceFactoryPostProcessor](we-autoconfigure%2Fsrc%2fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fatomic%2FAtomicAwareNodeBlockingServiceFactoryPostProcessor.kt) 
used by [AtomicAwareNodeBlockingServiceFactoryAutoConfiguration](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fatomic%2FAtomicAwareNodeBlockingServiceFactoryAutoConfiguration.kt).
#### Schema of wrapping order 
![schema_of_warappers.svg](puml%2Fschema_of_warappers.svg)  
[schema_of_warappers.puml](puml%2Fschema_of_warappers.puml)

#### Schema showing order of autoconfiguration for wrappers
An important task of this configuration order is to provide services to the node after being processed by all NodeBlockingServiceFactory wrappers.
![order_of_autoconfiguration_wrappers.svg](puml%2Forder_of_autoconfiguration_wrappers.svg)
[order_of_autoconfiguration_wrappers.puml](puml%2Forder_of_autoconfiguration_wrappers.puml)
##### Example of creating a CustomNodeBlockingServiceFactory
Kotlin:
```kotlin
@Configuration
@AutoConfigureBefore(NodeServicesAutoConfiguration::class)
@AutoConfigureAfter(AtomicAwareNodeBlockingServiceFactoryAutoConfiguration::class)
class CustomNodeBlockingServiceFactoryConfiguration {
    
    @Bean
    fun customNodeBlockingServiceFactoryPostProcessor(): CustomNodeBlockingServiceFactoryPostProcessor =
        CustomNodeBlockingServiceFactoryPostProcessor()
}

class CustomNodeBlockingServiceFactoryPostProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = when (bean) {
        is NodeBlockingServiceFactory -> CustomNodeBlockingServiceFactory(
            nodeBlockingServiceFactory = bean,
        )
        else -> bean
    }
}

class CustomNodeBlockingServiceFactory(
    val nodeBlockingServiceFactory: NodeBlockingServiceFactory,
): NodeBlockingServiceFactory by nodeBlockingServiceFactory {
    // overridden methods for services with extended behaviour
}
```
Java:
```java
@Configuration
@AutoConfigureBefore(NodeServicesAutoConfiguration.class)
@AutoConfigureAfter(AtomicAwareNodeBlockingServiceFactoryAutoConfiguration.class)
class CustomNodeBlockingServiceFactoryConfiguration {
    @Bean
    public CustomNodeBlockingServiceFactoryPostProcessor customNodeBlockingServiceFactoryPostProcessor() {
        new CustomNodeBlockingServiceFactoryPostProcessor();
    }
}

class CustomNodeBlockingServiceFactoryPostProcessor extends BeanPostProcessor {
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        if (bean instanceof NodeBlockingServiceFactory) {
            result = new CustomNodeBlockingServiceFactory((NodeBlockingServiceFactory) bean);
        }
        return result;
    }
}

class CustomNodeBlockingServiceFactory implements NodeBlockingServiceFactory {
    NodeBlockingServiceFactory delegate;


    public CustomNodeBlockingServiceFactory(NodeBlockingServiceFactory nodeBlockingServiceFactory) {
        this.delegate = nodeBlockingServiceFactory;
    }
    
    // overridden methods for services with extended behaviour
    // other services which are going to be used as is should be invoked via deleage
}
```

The main part here in terms of wrappers ordering is 
```java
@AutoConfigureBefore(NodeServicesAutoConfiguration.class)
@AutoConfigureAfter(AtomicAwareNodeBlockingServiceFactoryAutoConfiguration.class)
class CustomNodeBlockingFactoryConfiguration {

}
```

The `NodeServicesAutoConfiguration` is the finalizing autoconfiguration which builds the services to be used in the code, 
so every custom wrapper added should be configured before it.

If you want to wrap the services before the atomic logic, then you should implement your autoconfiguration like that:
```java
@AutoConfigureBefore(AtomicAwareNodeBlockingServiceFactoryAutoConfiguration.class)
@AutoConfigureAfter(NodeBlockingServiceFactoryAutoConfiguration.class)
class CustomNodeBlockingFactoryConfiguration {

}
```


## we-starter-contract-client
Contract client starter for invoking WE Docker Smart contracts from the backend applications.  
It has the following settings:
```yaml
contracts:
  myContract:
    contractId: # ID of the contract if not specified when invoking @ContractCall methods
    version: 3 # Version of the contract
    fee: 0  # Commission fee
    image: registry.com/example-contract:0.1 # Image of the contract's Docker image for calling @ContractCreate method
    imageHash: 76e00e0726794e2039467e887b27ba9a4afe9c41c6a6f316702db7d674b6d773 # Image hash of the contract's Docker image for calling @ContractCreate method
    validationEnabled: true # Property for switching the local validation feature - invoke contract's code inside the app using real state from the WE Node. 
    autoUpdate: # properties for contract auto update feature
      enabled:
      contractCreatorAddress:
```
### Example of configuration and usage
For example, a contract **ExampleContract** was built and deployed to the WE Node 
(for more information, see the [we-contract-sdk](https://github.com/waves-enterprise/we-contract-sdk) and [we-contract-client](https://github.com/waves-enterprise/we-contract-sdk/blob/master/we-contract-sdk-client/README.md) documentation).  

Kotlin:
```kotlin
interface ExampleContract {

    @ContractInit
    fun create()

    @ContractAction
    fun call(string: String)
}

class ExampleContractImpl(
    val state: ContractState,
) : ExampleContract {

    override fun create() {}

    override fun call(string: String) {
        state.put("EXAMPLE", string)
    }
}
```
Java:
```java
public interface ExampleContract {
   @ContractInit
   void create();

   @ContractAction
   void call(String string);
}

public final class ExampleContractImpl implements ExampleContract {

    private ContractState state;

    public ContractState getState() {
        return this.state;
    }

    public ExampleContractImpl(ContractState state) {
        super();
        this.state = state;
    }
    
    public void create() {}

    public void call(String string) {
        this.state.put("EXAMPLE", string);
    }
    
}
```
#### Invoking the contract

To invoke this contract in your Spring application you need to do the following steps:
1. Add a dependency for the contract client starter:
Gradle:
```kotlin
implementation("com.wavesenterprise:we-starter-contract-client:$version")
```
Maven:
```xml
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-contract-client</artifactId>
    <version>${version}</version>
</dependency>
```
_**Note:** Also you need to add a dependency on the project with the interface and implementation of the contract._
2. Add settings for the contract in the configuration file:
```yaml
# or default configuration
contracts:
  exampleContract:
    contractId: E8RGhX4rRhwy3UhNCDzms1ZzkCjE1PnGz1bbA5csbCxz
    version: 3
    fee: 0
    image: registry.com/example-contract:0.0.1
    imageHash: 76e00e0726794e2039467e887b27ba9a4afe9c41c6a6f316702db7d674b6d773
    autoUpdate:
      enabled: false # disabled by default
      contractCreatorAddress: 3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX
```
_**Note:** When switching from vst-commons legacy libraries, it is necessary to add to `contracts.legacy-mode: true`_
3. After specifying the settings for contracts, you need to add a configuration annotated with `@EnableContracts` to your Spring application:  

Kotlin:
```kotlin
@EnableContracts(
    contracts = [
        Contract(
            // main values
            api = ExampleContract::class,
            impl = ExampleContractImpl::class,
            name = "exampleContract",

            // optional values
            txSigner = "",
            nodeBlockingServiceFactory = "",
            converterFactory = "",
            
            // true by default
            localValidationEnabled = false,
        )
    ]
)
class ExampleConfiguration {}
```
Java:
```java
@EnableContracts(
        contracts = {
                @Contract(
                        // main values
                        api = ExampleContract.class,
                        impl = ExampleContractImpl.class,
                        name = "exampleContract",

                        // optional values
                        nodeBlockingServiceFactoryBeanRef = "",
                        converterFactoryBeanRef = "",
                        txSignerBeanRef = ""
                )
        }
)
class ExampleConfiguration {}
```
Additionally, if necessary, you can specify the references to your custom bean implementations of `TxSigner`, `NodeBlockingServiceFactory` and `ConverterFactory`.
4. Using the contract:

Kotlin:
```kotlin
@Service
class ExampleService(
    private val contract: ContractBlockingClientFactory<ExampleContract>,
) {
    fun example(): Tx =
        contract.execute {
            it.call()
        }.tx
}
```
Java: 
```java
@Service
class ExampleService {
    @Autowired
    private ContractBlockingClientFactory<ExampleContract> contractClient;

    public Tx example(ContractId contractId) {
        return contractClient.executeContract(
                contractId, (ExampleContract exampleContract) -> {
                    exampleContract.call();
                    return null;
                }).getTx();
    }
}

```
The contract client call returns **ExecutionContext** which, depending on the contract method being called, contains **CreateContractTx**(103) or **CallContractTx**(104).

#### SignRequest customization

To edit the fields of the contract's transactions (103 and 104 transaction types) when a contract is called, you can add a bean of type **ContractSignRequestCustomizer** to the Spring Application Context.
[ContractSignRequestContractVersionCustomizer.kt](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fcontract%2Fcustomizer%2FContractSignRequestContractVersionCustomizer.kt)
is one of such beans. It is
used to set `contractVersion` field according to the actual one fetched from the `/contract/{id}/info` endpoint. 


## we-starter-atomic
A starter that allows you to add one or more transactions into one atomic transaction.
An atomic transaction puts other transactions in a container for their atomic execution.
List of supported transactions by atomic tx - [Waves Enterprise Documentation](https://docs.wavesenterprise.com/ru/latest/description/transactions/tx-list.html#atomic-transaction)  
_**Note:** To use `we-starter-atomic` you should already have beans of type `NodeBlockingServiceFactory` and `TxSigner` in your Spring Application Context.
You can add `we-starter-node-client` and `we-starter-tx-tigner` dependencies which provide these beans._
### Adding a dependency
```kotlin
implementation("com.wavesenterprise:we-starter-atomic:$version")
```
```xml
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-atomic</artifactId>
    <version>$version</version>
</dependency>
```
### Example of usage with the `@Atomic` annotation 
For example, the method is wrapped with the `@Atomic` annotation. 
Within the context of this method all broadcasted transactions will be placed in an in-memory container. 
At the end of the method's invocation all the transactions from the container will be signed and broadcasted 
to the WE Node included in a single AtomicTx.

```java
@Service
public class ExampleService {

    @Autowired
    private ContractBlockingClientFactory<ExampleContract> contract;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private TxService txService;


     @Atomic
    public void example() {

        // Create contract (103 tx)
        ExecutionContext createContractExecutionContext = contract.executeContract(null, exampleContract -> {
            exampleContract.create();
            return null;
        });

        // Call contract (104 tx)
        contract.executeContract(
                ContractId.fromTxId(createContractExecutionContext.getTx().getId()),
                exampleContract -> {
                    exampleContract.call("test");
                    return null;
                });

        // Send data (114 tx)
        PolicyDataHashTx policyDataHashTx = privacyService.sendData(
                new SendDataRequest() // broadcastTx should be false 
        );
        txService.broadcast(policyDataHashTx);
    }
}
```
```kotlin
@Service
class ExampleService(
    private val contract: ContractBlockingClientFactory<ExampleContract>,
    private val privacyService: PrivacyService,
    private val txService: TxService,
) {

    @Atomic
    fun example() {
        // Create contract (103 tx)
        val executionContext = contract.executeContract {
            it.create()
        }

        // Call contract (104 tx)
        contract.executeContract(contractId = executionContext.tx.id.contractId) {
            it.call("test")
        }

        // Send data (114 tx) without broadcasting it immediately
        val policyDataHashTx = privacyService.sendData(SendDataRequest(broadcastTx = false))
        txService.broadcast(policyDataHashTx)
    }
}
```
After executing the `example()` method an atomic transaction (120) will be executed with a container consisting of **CreateContractTx(103)**, **CallContractTx(104)**, **PolicyDataHashTx(114)**.
A transaction of this type is executed atomically which means - all or nothing.

### Usage with `AtomicBroadcaster`
Instead of the annotation you can directly use the `AtomicBroadcaster` bean and it's method `doInAtomic()`.
To do this, it is necessary to inject `AtomicBroadcaster` in the class where joining transactions in AtomicTx is necessary 
and broadcast all the transactions in the context of the `doInAtomic()` method.

Java

```java
public class ExampleClass {
    
    private final AtomicBroadcaster atomicBroadcaster;
    
    public ExampleClass(AtomicBroadcaster atomicBroadcaster) {
        this.atomicBroadcaster = atomicBroadcaster;
    }
    
    public void example() {
        atomicBroadcaster.doInAtomic(() -> {
                    ExecutionContext createContractExecutionContext = contract.executeContract(null, exampleContract -> {
                        exampleContract.create();
                        return null;
                    });

                    // Call contract (104 tx)
                    contract.executeContract(
                            ContractId.fromTxId(createContractExecutionContext.getTx().getId()), exampleContract -> {
                                exampleContract.call("test");
                                return null;
                            });

                    // Send data (114 tx)
                    PolicyDataHashTx policyDataHashTx = privacyService.sendData(
                            new SendDataRequest() // broadcastTx should be false 
                    );
                    txService.broadcast(policyDataHashTx);
                    return null;
                }
        );
    }
}
```
Kotlin

```kotlin
    fun example() {
        atomicBroadcaster.doInAtomic {
            // Create contract (103 tx)
            val executionContext = contract.executeContract {
                it.create()
            }

            // Call contract (104 tx)
            contract.executeContract(contractId = executionContext.tx.id.contractId) {
                it.call("test")
            }

            // Send data (114 tx) without broadcasting it immediately
            val policyDataHashTx = privacyService.sendData(SendDataRequest(broadcastTx = false))
            txService.broadcast(policyDataHashTx)
        }
    }
```

## we-starter-tx-signer
The starter is required for signing WE Node transactions (contract calls or any other transactions 
to be broadcasted to the WE Node).

To use it you need to do the following steps:
1. Add the dependency:
Gradle:
```kotlin
implementation("com.wavesenterprise:we-starter-tx-signer:$version")
```
Maven:
```xml
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-tx-signer</artifactId>
    <version>${version}</version>
</dependency>
```
This starter will add an implementation of TxSigner which uses WE Node API `TxService` 
to sign the transactions. So fot this case the Node's key store key pairs will be used.

2. To specify which address should be used add a bean of type `NodeAddressProvider` to the Spring Application Context. 
This implementation is used to determine the address on whose behalf to sign the transactions. 
This example shows ussender from a HTTP header:
```java
@Configuration
class ExampleAppConfiguration {

    @Bean
    public NodeAddressProvider nodeAddressProvider() {
        return new NodeAddressProvider() {
            public Address address() {
                return Address.fromBase58(getRequest().getHeader("sender"));
            }
        };
    }
}
```
```kotlin
@Configuration
class ExampleAppConfiguration {
    
    @Bean
    fun nodeAddressProvider() = object : NodeAddressProvider {
        override fun address(): Address {
            return Address.fromBase58(getRequest().getHeader("sender"))
        }
    }
}
```
More usual implementation for this would be the one getting sender address from the JWT token.

3. For determining a password for the address an implementation of `NodeCredentialsProvider` is used. 
`we-starter-tx-signer` provides a default implementation which gets passwords from the configuration properties
defined under `node.credentials-provider`.
Example of yml with passwords:
```yaml
node:
  credentials-provider:
    address1: password2
    address2: password2
    address3: null
    address4: ""
  config:
    node-0:
      url: http://localhost:6862
```
4. When using `we-contract-sdk-starter` the `TxSigner` implementation from the Spring Application Context
will be used. So you don't have to explicitly sign outgoing transactions when invoking `executeContract()`- 
everything will be done under the hood.
5. To sign the transactions directly you should inject a bean of type `TxSigner` to your bean. 
It is done like that:
```java
public class ExampleClass {
    
    private final TxSigner txSigner;
    private final TxService txService;
    
    public ExampleClass(TxSigner txSigner, TxService txService) {
        this.txSigner = txSigner;
        this.txService = txService;
    }
    
    public void example() {
        var createPolicyTxSignRequest = new CreatePolicySignRequest(); // sign request with necessary data
        CreatePolicyTx createPolicyTx = txSigner.sign(createPolicyTxSignRequest);
        txService.broadcast(createPolicyTx);
    }
}
    
```


## Links:
* [Waves Enterprise documentation](https://docs.wavesenterprise.com/ru/latest/)
