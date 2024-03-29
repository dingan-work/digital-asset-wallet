package wiki.ganhua.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 生成钱包相关信息
 *
 * @author Ganhua
 * @date ll022/2/9
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletResult {

    /**
     * 钱包地址
     */
    private String address;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 助记词
     */
    private List<String> mnemonics;

    public WalletResult(String address, String privateKey) {
        this.address = address;
        this.privateKey = privateKey;
    }
}
