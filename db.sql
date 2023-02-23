CREATE TABLE `t_instance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `app_code` varchar(256) NOT NULL DEFAULT '' COMMENT '服务编码',
  `app_name` varchar(256) NOT NULL DEFAULT '' COMMENT '应用服务名称,服务发现使用',
  `ip` varchar(128)  NOT NULL DEFAULT '' COMMENT 'ip',
  `env_type` varchar(128)   NOT NULL DEFAULT '' COMMENT '环境类型:dev、beta、product、online',
  `env_group` varchar(128) NOT NULL DEFAULT '' COMMENT '环境分组',
  `env_code` varchar(256)   NOT NULL DEFAULT '' COMMENT '环境唯一码，一般为：env_type + env_group',
  `cluster` varchar(256) NOT NULL DEFAULT '' COMMENT '实例所属集群',
  `version` varchar(128) NOT NULL DEFAULT '' COMMENT '实例部署的版本号',
  `reversion` bigint(20) NOT NULL DEFAULT '0' COMMENT '实例变化的版本号,是用来保证数据不被回滚',
  `provider` varchar(512) NOT NULL DEFAULT '' COMMENT '实例提供方,k8s,ecs',
  `cpu` int(12) NOT NULL DEFAULT '0' COMMENT 'cpu个数',
  `memory` int(12) NOT NULL DEFAULT '0' COMMENT '内存大小',
  `disk` int(12) NOT NULL DEFAULT '0' COMMENT '磁盘大小',
  `os` varchar(256) NOT NULL DEFAULT '' COMMENT '操作系统',
  `image` varchar(256) NOT NULL DEFAULT '' COMMENT '镜像',
  `label` varchar(1000) NOT NULL DEFAULT '' COMMENT '扩展信息',
  `hostname` varchar(256) NOT NULL DEFAULT '' COMMENT 'hostname',
  `idc` varchar(256) NOT NULL DEFAULT '' COMMENT '机房',
  `enabled` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否处于上线的状态',
  `state` varchar(64) NOT NULL DEFAULT '' COMMENT '实例状态',
  `health_state` varchar(64) NOT NULL DEFAULT '' COMMENT '健康监测状态',
  `status` tinyint(3) NOT NULL DEFAULT '0' COMMENT '实例状态   0:其它 1：实例可用 2：实例摘除',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_instance_id` (`instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='实例信息表';
     
 
CREATE TABLE `t_instance_port` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `name` varchar(128)   NOT NULL DEFAULT '' COMMENT '端口名',
  `port` varchar(8)   NOT NULL DEFAULT '' COMMENT 'port',
  `protocol` varchar(64) NOT NULL DEFAULT '' COMMENT '协议',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_port_instance_port` (`instance_id`, `port`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='实例端口表';
 
CREATE TABLE `t_instance_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `log_type`  tinyint(3)   NOT NULL DEFAULT '0' COMMENT '日志类型，新增、修改、删除',
  `enabled` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否处于上线的状态',
  `status` tinyint(3) NOT NULL DEFAULT '0' COMMENT '实例状态   0:其它 1：实例可用 2：实例摘除',
  `state` varchar(64) NOT NULL DEFAULT '' COMMENT '实例状态',
  `health_state` varchar(64) NOT NULL DEFAULT '' COMMENT '健康监测状态',
  `instance_info` varchar(1000) NOT NULL DEFAULT '' COMMENT '实例信息',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='实例kbs信息推送记录表';
 
CREATE TABLE `t_provider_service` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `service_name` varchar(512) NOT NULL DEFAULT '' COMMENT '服务名称',
  `service_group` varchar(128) NOT NULL DEFAULT '' COMMENT '服务分组',
  `service_version` varchar(128) NOT NULL DEFAULT '' COMMENT '服务版本号',
  `service_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'spring cloud /dubbo',
  `register_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '注册中心类型，zk\nacos',
  `protocol` varchar(256) NOT NULL DEFAULT '' COMMENT '协议',
  `metadata` varchar(1000) NOT NULL DEFAULT '' COMMENT '扩展数据',
  `service_key` varchar(128) NOT NULL DEFAULT '' COMMENT '服务key',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_provider_service_key` (`service_key`),
  KEY `idx_provider_service_instance_id` (`instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='提供方信息表';
  
CREATE TABLE `t_consumer_service` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `service_name` varchar(512) NOT NULL DEFAULT '' COMMENT '服务名称',
  `service_group` varchar(128) NOT NULL DEFAULT '' COMMENT '服务分组',
  `service_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'spring cloud /dubbo',
  `register_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '注册中心类型，zk\nacos',
  `protocol` varchar(256) NOT NULL DEFAULT '' COMMENT '协议',
  `metadata` varchar(1000) NOT NULL DEFAULT '' COMMENT '扩展数据',
  `service_version` varchar(128) NOT NULL DEFAULT '' COMMENT '服务版本号',
  `service_key` varchar(128) NOT NULL DEFAULT '' COMMENT '服务key',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_consumer_service_key` (`service_key`),
  KEY `idx_consumer_service_instance_id` (`instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='消费方信息表';
 
CREATE TABLE `t_gateway_instance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `instance_id` varchar(128)   NOT NULL DEFAULT '' COMMENT '实例id',
  `ip` varchar(128)  NOT NULL DEFAULT '' COMMENT 'ip',
  `port` varchar(64)   NOT NULL DEFAULT '' COMMENT 'port',
  `env_type` varchar(64)   NOT NULL DEFAULT '' COMMENT '环境类型:dev、beta、product、online',
  `enabled` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否启用此实例',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_gateway_instance_id` (`instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET = utf8mb4 COMMENT='网关实例信息表';
