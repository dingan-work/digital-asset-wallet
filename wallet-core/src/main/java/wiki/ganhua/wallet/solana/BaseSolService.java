package wiki.ganhua.wallet.solana;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import wiki.ganhua.bean.WalletResult;
import wiki.ganhua.exception.AddressException;
import wiki.ganhua.service.WalletService;
import wiki.ganhua.wallet.RpcPar;
import wiki.ganhua.wallet.solana.model.PublicKey;
import wiki.ganhua.wallet.solana.model.SolTransaction;
import wiki.ganhua.wallet.solana.programs.SystemProgram;
import wiki.ganhua.wallet.solana.rpc.ConfirmedForHash;
import wiki.ganhua.wallet.solana.rpc.ConfirmedTransaction;
import wiki.ganhua.wallet.solana.rpc.RecentBlockHash;
import wiki.ganhua.wallet.solana.rpc.RpcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * refer to the interface documentation：https://docs.solana.com/developing/clients/jsonrpc-api
 *
 * @author Ganhua
 * @date 2022/2/14
 */
public abstract class BaseSolService implements WalletService {

    @Override
    public WalletResult createWalletAddress(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 4];
        secureRandom.nextBytes(entropy);
        // 生成12位助记词
        List<String> mnemonic;
        try {
            mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new AddressException("wallet address generation failed");
        }
        // 秘钥为空
        SolAccount solAccount = SolAccount.fromMnemonic(mnemonic, StrUtil.EMPTY);
        return new WalletResult(solAccount.getPublicKey().toString(),
                Base58.encode(solAccount.getSecretKey()),mnemonic);
    }

    @Override
    public BigDecimal getBalance(String address) {
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method("getBalance")
                .params(List.of(address)).build();
        JSONObject response = RpcClient.call(par, JSONObject.class);
        BigDecimal balance = response.getBigDecimal("value");
        // sol precision 9
        return balance.compareTo(BigDecimal.ZERO)==0 ? balance : balance.divide(BigDecimal.TEN.pow(9),6, RoundingMode.DOWN);
    }

    @Override
    public String sendTransaction(String to, BigDecimal amount, String from, String privateKey) {
        PublicKey fromPublicKey = new PublicKey(from);
        PublicKey toPublicKey = new PublicKey(to);
        SolAccount signer = new SolAccount(Base58.decode(privateKey));
        SolTransaction transaction = new SolTransaction();
        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublicKey, amount.multiply(BigDecimal.TEN.pow(9)).longValue()));
        return sendTransaction(transaction, Collections.singletonList(signer));
    }

    private String sendTransaction(SolTransaction transaction, List<SolAccount> signers) {
        String recentBlockHash = getRecentBlockHash();
        transaction.setRecentBlockHash(recentBlockHash);
        transaction.sign(signers);
        byte[] serializedTransaction = transaction.serialize();
        String base64Trx = Base64.getEncoder().encodeToString(serializedTransaction);
        List<Object> params = new ArrayList<>();
        params.add(base64Trx);
        params.add(Dict.create().set("encoding","base64"));
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method("sendTransaction")
                .params(params).build();
        return RpcClient.call(par, String.class);
    }

    /**
     * 从账本返回最近的区块哈希值
     */
    public static String getRecentBlockHash() {
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method("getRecentBlockhash")
                .params(null).build();
        return RpcClient.call(par, RecentBlockHash.class).getBlockHash();
    }

    /**
     * query the most recent transaction based on the address
     * and start the search backward from a transaction signature
     */
    public static List<ConfirmedForHash> getConfirmedSignaturesForAddress(String address, String hashStr){
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method("getConfirmedSignaturesForAddress2")
                .params(List.of(address,Dict.create().set("until",hashStr))).build();
        return Convert.convert(new TypeReference<>() {},RpcClient.call(par,List.class));
    }

    /**
     * returns the details of a transaction hash
     */
    public static ConfirmedTransaction getConfirmedTransaction(String hashStr){
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method("getConfirmedTransaction")
                .params(List.of(hashStr,"json")).build();
        return RpcClient.call(par,ConfirmedTransaction.class);
    }
}
