package wiki.ganhua.wallet.web3j;

import com.google.common.collect.ImmutableList;
import wiki.ganhua.exception.AddressException;
import wiki.ganhua.service.WalletService;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import wiki.ganhua.bean.WalletResult;

import java.security.SecureRandom;
import java.util.List;

/**
 * web3j处理只能合约的通用调用接口
 *
 * @author Ganhua
 * @date 2022/2/9
 */
public abstract class BaseWeb3jService implements WalletService {

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


}
