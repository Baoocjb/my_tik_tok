package com.bao.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Component
// 多例
@Scope(ConfigurableListableBeanFactory.SCOPE_PROTOTYPE)
public class RedisDataSoureceTransaction {
    @Autowired
    private RedisOperator redisUtil;
    /**
     * 数据源事务管理器
     */
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    /**
     * 开始事务 采用默认传播行为
     *
     * @return
     */
    public TransactionStatus begin() {
        // 手动begin数据库事务
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(new DefaultTransactionAttribute());
        redisUtil.begin();
        return transaction;
    }
    /**
     * 提交事务
     *
     * @param transactionStatus
     * 事务传播行为
     * @throws Exception
     */
    public void commit(TransactionStatus transactionStatus) throws Exception {
        if (transactionStatus == null) {
            throw new Exception("transactionStatus is null");
        }
        // 支持Redis与数据库事务同时提交
        dataSourceTransactionManager.commit(transactionStatus);
        redisUtil.exec();
    }
    /**
     * 回滚事务
     *
     * @param transactionStatus
     * @throws Exception
     */
    public void rollback(TransactionStatus transactionStatus) throws Exception {
        if (transactionStatus == null) {
            throw new Exception("transactionStatus is null");
        }
        dataSourceTransactionManager.rollback(transactionStatus);
        redisUtil.discard();
    }
}
