package wiki.ganhua.wallet.web3j;

import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import wiki.ganhua.exception.CommonException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * web3j 通用方案
 *
 * @author Ganhua
 * @date 2022/2/14
 */
@Slf4j
public class Web3jCommon {

    /**
     * 获取地址nonce || 获取地址交易笔数
     * 详情可参考：https://www.ganhua.work/archives/web3j-nonce.html
     * @param address 地址
     */
    public BigInteger getNonce(Web3j web3j, String address){
        BigInteger nonce;
        EthGetTransactionCount ethGetTransactionCount;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
        } catch (IOException e) {
            throw new CommonException("failed to get the number of sent transactions",e.getMessage());
        }
        if (ethGetTransactionCount == null) {
            throw new CommonException("the result of getting the nonce on the chain is null");
        }
        nonce = ethGetTransactionCount.getTransactionCount();
        return nonce;
    }

    /**
     * 获取当前交易需要的gas费用
     * @param gasLimit user settings Fee level The higher the probability of being packaged by miners
     */
    public BigInteger getGas(Web3j web3j,BigInteger gasLimit) {
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            BigInteger gasPrice = new BigDecimal(ethGasPrice.getGasPrice()).multiply(new BigDecimal("1.1")).toBigInteger();
            return gasPrice.multiply(gasLimit);
        } catch (IOException e) {
            log.error("查询当前矿工费失败，{}",e.getMessage());
            return new BigInteger("0.0005");
        }
    }

    /**
     * 解析input地址
     * @param inputData input data
     */
    public String hexToAddress(String inputData) {
        String strHex = inputData.substring(10, 74);
        boolean ox = '0' == strHex.charAt(0) && ('X' == strHex.charAt(1) || 'x' == strHex.charAt(1));
        if (ox) {
            strHex = strHex.substring(2);
        }
        strHex = strHex.substring(24);
        return "0x" + strHex;
    }

    /**
     * 合约解析交易金额 保留8位小数
     * @param inputData input data
     * @param precision 精度
     * @return 交易金额 解析失败返回 0
     */
    public BigDecimal hexToAmount(String inputData, BigDecimal precision) {
        String strHex = inputData.substring(74);
        if (strHex.length() > 2) {
            boolean ox = '0' == strHex.charAt(0) && ('X' == strHex.charAt(1) || 'x' == strHex.charAt(1));
            if (ox) {
                strHex = strHex.substring(2);
            }
            return new BigDecimal(new BigInteger(strHex, 16)).divide(precision,8, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }
}
