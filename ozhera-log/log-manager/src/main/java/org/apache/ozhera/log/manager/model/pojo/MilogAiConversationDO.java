package org.apache.ozhera.log.manager.model.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("milog_ai_conversation")
public class MilogAiConversationDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Comment("Primary key Id")
    @ColDefine(customType = "bigint")
    private Long id;

    @Column(value = "store_id")
    @ColDefine(customType = "bigint")
    @Comment("store id")
    private Long storeId;

    @Column(value = "conversation_context")
    @ColDefine(type = ColType.TEXT)
    @Comment("The conversation context used by llm")
    private String conversationContext;

    @Column(value = "original_conversation")
    @ColDefine(type = ColType.TEXT)
    @Comment("The original context with llm")
    private String originalConversation;

    @Column(value = "creator")
    @ColDefine(type = ColType.VARCHAR)
    @Comment("creator")
    private String creator;

    @Column(value = "create_time")
    @ColDefine(customType = "bigint")
    @Comment("create time")
    private Long createTime;

    @Column(value = "update_time")
    @ColDefine(customType = "bigint")
    @Comment("update time")
    private Long updateTime;
}
