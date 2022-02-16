package wiki.ganhua.wallet.web3j.token;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;
import wiki.ganhua.exception.AddressException;
import wiki.ganhua.exception.TransactionException;
import wiki.ganhua.service.WalletService;
import wiki.ganhua.wallet.web3j.Web3jCommon;
import wiki.ganhua.wallet.web3j.model.Web3jTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * web3j token related solutions
 *
 * @author Ganhua
 * @date 2022/2/14
 */
@Slf4j
public abstract class BaseWeb3jTokensService extends Web3jCommon implements WalletService {

    public String sendTransaction(Web3j web3j, Web3jTransaction transaction) {
        BigInteger nonce = getNonce(web3j, transaction.getFromAddress());
        BigInteger gasPrice = getGas(web3j, transaction.getGasLimit());
        //token转账参数
        String methodName = "transfer";
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address tAddress = new Address(transaction.getToAddress());
        Uint256 tokenValue = new Uint256(transaction.getAmount().multiply(BigDecimal.TEN.pow(getTokenDecimals(web3j, transaction.getContractAddress()))).toBigInteger());
        TypeReference<Bool> typeReference = new TypeReference<>() {};
        outputParameters.add(typeReference);
        Function function = new Function(methodName, Arrays.asList(tAddress,tokenValue), outputParameters);
        String data = FunctionEncoder.encode(function);
        String signedData;
        try {
            signedData = signTransaction(nonce, gasPrice, transaction.getGasLimit(), transaction.getContractAddress(), data, transaction.getChainId(), transaction.getFromPrivateKey());
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).sendAsync().get();
            if (ethSendTransaction.getTransactionHash() == null) {
                log.error("web3j-tokens transaction failed,{},{}", ethSendTransaction.getError().getMessage(), transaction);
            }
            return ethSendTransaction.getTransactionHash();
        } catch (Exception e) {
            throw new TransactionException("web3j tokens transaction signing failed");
        }
    }

    /**
     * offline signed transactions
     */
    private String signTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, String data, long chainId, String privateKey) {
        byte[] signedMessage;
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data);
        boolean ox = '0' == privateKey.charAt(0) && ('X' == privateKey.charAt(1) || 'x' == privateKey.charAt(1));
        if (ox) {
            privateKey = privateKey.substring(2);
        }
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);
        if (chainId > -1) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }
        return Numeric.toHexString(signedMessage);
    }

    /**
     * 查询代币精度 精度需要缓存下来
     * @param contractAddress 合约地址
     * @return 精度
     */
    public int getTokenDecimals(Web3j web3j,String contractAddress) {
        String methodName = "decimals";
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Uint8> typeReference = new TypeReference<>() {};
        outputParameters.add(typeReference);
        Function function = new Function(methodName, List.of(), outputParameters);
        String data = FunctionEncoder.encode(function);
        String emptyAddress = "0x0000000000000000000000000000000000000000";
        Transaction transaction = Transaction.createEthCallTransaction(emptyAddress, contractAddress, data);
        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if (results.size() > 0) {
                return Integer.parseInt(results.get(0).getValue().toString());
            }
            throw new AddressException("precision not obtained");
        } catch (InterruptedException | ExecutionException e) {
            throw new AddressException("contract query precision failed");
        }
    }

}
