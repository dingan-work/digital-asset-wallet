package wiki.ganhua.wallet.solana;

import cn.hutool.core.util.StrUtil;
import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import wiki.ganhua.bean.WalletResult;
import wiki.ganhua.exception.AddressException;
import wiki.ganhua.service.WalletService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

/**
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

        return null;
    }
}
