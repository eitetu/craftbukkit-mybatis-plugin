package com.eitetu.craftbukkit.mybatis;

import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MybatisPlugin extends JavaPlugin {
	private static SqlSessionManager sqlSessionManager;

	@Override
	public void onEnable() {
		super.onEnable();

		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();

		ConfigurationSection sections;

		sections = config.getConfigurationSection("database");

		final PooledDataSourceFactory dataSourceFactory;
		final TransactionFactory transactionFactory;
		final SqlSessionFactory sqlSessionFactory;

		dataSourceFactory = new PooledDataSourceFactory();

		if(sections != null) {
			Set<String> keys = sections.getKeys(true);
			Properties dataSourceProperties = new Properties();

			for(String key : keys) {
				dataSourceProperties.setProperty(key, sections.getString(key));
			}

			dataSourceFactory.setProperties(dataSourceProperties);
		}

		transactionFactory = new JdbcTransactionFactory();

		Environment environment = new Environment("product", transactionFactory, dataSourceFactory.getDataSource());
		Configuration configuration = new Configuration(environment);

		sections = config.getConfigurationSection("session");
		SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);
		sqlSessionManager = SqlSessionManager.newInstance(sqlSessionFactory);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		if(sqlSessionManager != null)
			sqlSessionManager.close();

		sqlSessionManager = null;
	}

	public <T> T addMapper(Class<T> mapper) {
		if(sqlSessionManager.getConfiguration().hasMapper(mapper) == false) {
			sqlSessionManager.getConfiguration().addMapper(mapper);
		}

		return getMapper(mapper);
	}

	public <T> T getMapper(Class<T> mapper) {
		return sqlSessionManager.getMapper(mapper);
	}
}
