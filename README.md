# we-sdk-spring

Java/Kotlin SDK starter with autoconfigurations for contract and node client.
Also contains autoconfigurations for signing SignRequests via tx-signer and logic for atomics.

## Основное
To start work, you need to add a dependency on the starter that you need to your project. \
List of possible dependencies:
* [we-starter-atomic](we-starters%2Fwe-starter-atomic)
* [we-starter-contract-client](we-starters%2Fwe-starter-contract-client)
* [we-starter-node-client](we-starters%2Fwe-starter-node-client)
* [we-starter-tx-signer](we-starters%2Fwe-starter-tx-signer)

## we-starter-node-client
Стартер, предоставляющий отдельные сервисы для взаимодействия с нодой. \
Список сервисов: TxService, ContractService, AddressService, NodeInfoService, PrivacyService, BlocksService, BlockchainEventsService, UtilsService. Названия сервисов совпадают с ендпоинтами api ноды. 
Основным элементом стартера является интерфейс `NodeBlockingServiceFactory`, который позволяет оборачивать и перепределять методы сервисов (подробнее в документации `we-node-client`). \
### Добавление зависимости
Gradle:
```
implementation("com.wavesenterprise:we-starter-node-client")
```
Maven:
```
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-node-client</artifactId>
    <version>$version</version>
</dependency>
```
### Настройка
Before you start working with `we-starter-node-client`, you need to set values in the configuration file for working with the node. \
The main setting is [NodeProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FNodeProperties.kt) \
_Note:_ When switching from vst client, you need to add `node.config.node-0.legacy-mode: true`
#### Example properties
```yaml
node:
  config:
    node-0:
      http:
        url: http://localhost:8080/node-0/
        xApiKey: key
        xPrivacyApiKey: key
        feign:
          decode404: true
          connectTimeout: 5000
          readTimeout: 5000
          loggerLevel: FULL
  addresses:
    address: password # {address}: {password}
```
If necessary, you can configure the following default wrappers for NodeBlockingServiceFactory:
* **RateLimitingServiceFactory** - configured in [RateLimiterProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FRateLimiterProperties.kt);
```yaml
node:
  rate-limiter: # example with default values
    enabled: true
    maxUtx: 50 # value for limit requests if utx pool will be overflowing
    minWaits: 1s # minimum waiting time for a repeated request to the node
    maxWait: 3s #
    maxWaitTotal: 10s
```
* **CachingNodeBlockingServiceFactory** - configured in [CacheProperties](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fproperties%2FCacheProperties.kt). 
```yaml
node:
  cache: # example with default values
    enabled: true
    txCacheSize: 5000 # maximum number of transactions allowed to be stored in cache
    policyItemInfoCacheSize: 500 # maximum number of policy item info allowed to be stored in cache
    cacheDuration: 500s # time limit on cache storage
```
* **LoadBalancingServiceFactory** - Does not have separate settings. Works when clients are configured to more than one node; \

_Note:_ For more information about wrappers, see the `we-node-client` documentation.

An important element of this autoconfiguration is that in a separate configuration are the settings of the node services [NodeServicesAutoConfiguration](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fnode%2Fservice%2FNodeServicesAutoConfiguration.kt) . This configuration is performed after all wrappers for `NodeServiceFactory` counting the spring annotation `@AutoConfigureBefore(NodeServicesAutoConfiguration::class)` \
This allows you to add client wrappers in client code via postprocessor and use node services via bean injection without a full client (An example of implementing a wrapper for atomics [AtomicAwareNodeBlockingServiceFactoryPostProcessor](we-autoconfigure%2Fsrc%2fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fatomic%2FAtomicAwareNodeBlockingServiceFactoryPostProcessor.kt)) in [AtomicAwareNodeBlockingServiceFactoryAutoConfiguration](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fatomic%2FAtomicAwareNodeBlockingServiceFactoryAutoConfiguration.kt).
### Schema of wrappers @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
## we-starter-contract-client
Contract client starter for working with contracts. \
It has the following settings:
```yaml
contracts:
  contract-1:
    contractId: 
    version: 3
    fee: 0 
    image: registry.com/example-contract:0.1
    imageHash: 76e00e0726794e2039467e887b27ba9a4afe9c41c6a6f316702db7d674b6d773
    autoUpdate:
      enabled:
      contractCreatorAddress:
```
### Example of connection, configuration and usage:
For example, a contract **ExampleContract** was created on the node (for more information, see the documentation **we-contract-sdk**). \
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

```

To use it by the client, you need to do the following steps:
1. Adding a dependency to the client starter:
Gradle:
```
implementation("com.wavesenterprise:we-starter-contract-client:$version")
// also, if necessary, you need to add a dependency on the project with the interface and implementation of the contract
```
Maven:
```
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-contract-client</artifactId>
    <version>$version</version>
</dependency>
// also, if necessary, you need to add a dependency on the project with the interface and implementation of the contract
```
2. Add settings for the contract to the configuration file:
```yaml
# or default configuration
contracts:
  example-contract:
    contractId: E8RGhX4rRhwy3UhNCDzms1ZzkCjE1PnGz1bbA5csbCxz
    version: 3
    fee: 0
    image: registry.com/example-contract:0.0.1
    imageHash: 76e00e0726794e2039467e887b27ba9a4afe9c41c6a6f316702db7d674b6d773
    autoUpdate:
      enabled: false # disabled by default
      contractCreatorAddress: 3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX
```
_Note:_ When switching from vst-client, it is necessary to add to `contracts.legacy-mode: true`
3. After specifying the settings for contracts, you need to add the @EnableContracts annotation to the main class or to the configuration class in your spring application: \
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
                        nodeBlockingServiceFactory = "",
                        converterFactory = "",
                        txSigner = "",

                        // true by default
                        localValidationEnabled = false
                )
        }
)
class ExampleConfiguration {}
```
Additionally, if necessary, you can specify the names of your custom beans for `TxSigner, NodeBlockingServiceFactory, ConverterFactory` and disable local validation when calling the contract (does not send a transaction if validation fails).
4. Using the contract: \
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
The contract client call returns **ExecutionContext**, which, depending on the contract method being called, is returned with **CreateContractTx**(103) or **CallContractTx**(104).
5. Дополнительно \
Для реадктирования sign request, который отправляется при вызовах контракта, можно реализовать **ContractSignRequestCustomizer**. Пример использования [ContractSignRequestContractVersionCustomizer.kt](we-autoconfigure%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fwavesenterprise%2Fsdk%2Fspring%2Fautoconfigure%2Fcontract%2Fcustomizer%2FContractSignRequestContractVersionCustomizer.kt) (Здача этого кастомайзера заключается в использовании активного контракта и его актульной версии).
Для собственной реализации необходимо добавить в spring context bean of your implementation from **ContractSignRequestCustomizer**. 


## we-starter-atomic
A starter that allows you to add one or more transactions into one atomic transaction.
An atomic transaction puts other transactions in a container for their atomic execution.
List of supported transactions by atomic tx - [Waves Enterprise Documentation](https://docs.wavesenterprise.com/ru/latest/description/transactions/tx-list.html#atomic-transaction) \
_Note_: to use `we-starter-atomic`, you need to add dependencies on `we-starter-node-client` and `we-starter-tx-tigner` (if there is a custom `nodeBlockingServiceFactory` and `txSigner` in the context - optional).
### Adding a dependency
```
implementation("com.wavesenterprise:we-starter-atomic")
```
```
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-atomic</artifactId>
    <version>$version</version>
</dependency>
```
### Example of use with the annotation `@Atomic`
For example, the method is wrapped with the @Atomic annotation. Within the framework of this method, signed or sent transactions will be placed in the transaction container 120, in turn, it will be executed atomically upon completion of the execution of the method.
```java
@Service
public class ExampleService {
    @Autowired
    private ContractBlockingClientFactory<ExampleContract> contract;
    @Autowired
    private PrivacyService privacyService;

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
        privacyService.sendData(new SendDataRequest());
    }
}
```
```kotlin
@Service
class ExampleService(
    private val contract: ContractBlockingClientFactory<ExampleContract>,
    private val privacyService: PrivacyService,
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

        // Send data (114 tx)
        privacyService.sendData(SendDataRequest())
    }
}
```
After executing the `example()` method an atomic transaction (120) will be executed with a container consisting of **CreateContractTx(103)**, **CallContractTx(104)**, **PolicyDataHashTx(114)**.
A transaction of this type is executed in full (none of the included transactions is rejected) or is not executed in principle.
### Example of use with `AtomicBroadcaster`
Instead of an annotation, you can directly use the implementation of `AtomicBroadcaster.doInAtomic()` (the implementation is used by the annotation).
To do this, it is necessary to inject `AtomicBroadcaster` in the class where the atomic tx is needed and call `doInAtomic()` on it
```java
    public void example() {
        atomicBroadcaster.doInAtomic(
                block -> {
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
                    privacyService.sendData(new SendDataRequest());
                    return null;
                }
        );
    }
```
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

            // Send data (114 tx)
            privacyService.sendData(SendDataRequest())
        }
    }
```

## we-starter-tx-signer
The starter is required for signing transactions (contract calls, direct signings and sending transactions to the node).

To use it, you need to do the following steps:
1. Adding a dependency:
Gradle:
```
implementation("com.wavesenterprise:we-starter-tx-signer:$version")
```
Maven:
```
<dependency>
    <groupId>com.wavesenterprise</groupId>
    <artifactId>we-starter-tx-signer</artifactId>
    <version>$version</version>
</dependency>
```
2. Adding a custom bean to the application context via the configuration to determine the address on whose behalf to sign transactions. For example, receiving a sender from a header:
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
3. Further, each time contracts are called or transactions are signed directly, the transaction will be signed with the sender's address specified in NodeAddressProvider and the password defined in `node.credentials-provider`.

## Links:
* [Waves Enterprise documentation](https://docs.wavesenterprise.com/ru/latest/)
