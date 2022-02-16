package wiki.ganhua.wallet.web3j;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import wiki.ganhua.exception.AddressException;
import wiki.ganhua.exception.CommonException;
import wiki.ganhua.exception.TransactionException;
import wiki.ganhua.service.WalletService;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import wiki.ganhua.bean.WalletResult;
import wiki.ganhua.wallet.web3j.model.Web3jTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * web3j处理只能合约的通用调用接口
 *
 * @author Ganhua
 * @date 2022/2/9
 */
@Slf4j
public abstract class BaseWeb3jService extends Web3jCommon implements WalletService {

    /**
     * 根据币种配置信息创建钱包
     */
    protected WalletResult createWallet(int childNumber){
        ImmutableList<ChildNumber> bipPath = ImmutableList.of(new ChildNumber(44, true), new ChildNumber(childNumber, true),
                ChildNumber.ZERO_HARDENED, ChildNumber.ZERO);
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);
        //生成12位助记词
        List<String> str;
        try {
            str = MnemonicCode.INSTANCE.toMnemonic(entropy);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new AddressException("wallet address generation failed");
        }
        //使用助记词生成钱包种子
        byte[] seed = MnemonicCode.toSeed(str, "");
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        DeterministicKey deterministicKey = deterministicHierarchy
                .deriveChild(bipPath, false, true, new ChildNumber(0));
        byte[] bytes = deterministicKey.getPrivKeyBytes();
        ECKeyPair keyPair = ECKeyPair.create(bytes);
        //通过公钥生成钱包地址
        String address = Keys.getAddress(keyPair.getPublicKey());
        return new WalletResult("0x"+address,keyPair.getPrivateKey().toString(16),str);
    }


    /**
     * web3j 主币转账
     */
    public String sendTransaction(Web3j web3j, Web3jTransaction transaction){
        BigInteger nonce = getNonce(web3j, transaction.getFromAddress());
        BigInteger gasPrice = getGas(web3j, transaction.getGasLimit());
        //转账人私钥
        Credentials credentials = Credentials.create(transaction.getFromPrivateKey());
        //创建交易
        BigInteger value = Convert.toWei(transaction.getAmount(), Convert.Unit.ETHER).toBigInteger();
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, transaction.getGasLimit(), transaction.getToAddress(), value);
        //签名Transaction，这里要对交易做签名
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,transaction.getChainId(),credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        //发送交易
        EthSendTransaction ethSendTransaction;
        try {
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("交易失败,{}",e.getMessage());
            throw new TransactionException("the transaction failed to be uploaded to the chain, and the network was abnormal");
        }
        Assert.notNull(ethSendTransaction,"transaction failed,the transaction result is null");
        String transactionHash = ethSendTransaction.getTransactionHash();
        if (transactionHash==null){
            log.error("web3j转账失败，交易：{},错误信息：{}",transaction,ethSendTransaction.getError().getMessage());
            throw new TransactionException(ethSendTransaction.getError().getMessage());
        }
        return transactionHash;
    }

    /**
     * 获取地址余额 默认所有主币精度都是18
     * @param address 地址
     * @return 余额
     */
    public BigDecimal getBalance(Web3j web3j,String address){
        EthGetBalance ethGetBalance;
        try {
            ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            log.error("failed to get address amount: address:{}",address);
            throw new CommonException("getting web3j balance failed");
        }
        return Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()), Convert.Unit.ETHER);
    }
}
