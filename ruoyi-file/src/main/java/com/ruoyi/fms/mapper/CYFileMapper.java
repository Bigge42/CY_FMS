package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFile;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CYFileMapper {


        // 根据 matchID、documentTypeIDs 和 PlanTrackingNumber 查询文件ID集合
        @Select("<script>" +
                "SELECT fileID, documentTypeID " +
                "FROM cy_file " +
                "WHERE matchID = #{matchID} " +
                "AND planTrackingNumber = #{planTrackingNumber} " +
                "AND documentTypeID IN " +
                "<foreach collection='documentTypeIDs' item='item' separator=',' open='(' close=')'>" +
                "#{item}" +
                "</foreach> " +
                "AND deleteFlag = 0" +
                "</script>")
        List<Map<String, Object>> findFileIDsByMatchIDAndDocumentTypeIDsAndPlanTrackingNumber(
                @Param("matchID") String matchID,
                @Param("documentTypeIDs") List<Integer> documentTypeIDs,
                @Param("planTrackingNumber") String planTrackingNumber);

        // 根据 matchID 和 documentTypeID 和 PlanTrackingNumber 查询文件ID集合
        @Select("SELECT fileID FROM cy_file WHERE matchID = #{matchID} AND documentTypeID = #{documentTypeID} AND planTrackingNumber = #{planTrackingNumber} AND deleteFlag = 0")
        List<String> findFileIDsByMatchIDAndDocumentTypeIDAndPlanTrackingNumber(@Param("matchID") String matchID,
                                                                                @Param("documentTypeID") Integer documentTypeID,
                                                                                @Param("planTrackingNumber") String planTrackingNumber);

        // 根据 matchID 和 PlanTrackingNumber 查询文件ID集合
        @Select("SELECT fileID FROM cy_file WHERE matchID = #{matchID} AND planTrackingNumber = #{planTrackingNumber} AND deleteFlag = 0")
        List<String> findFileIDsByMatchIDAndPlanTrackingNumber(@Param("matchID") String matchID,
                                                               @Param("planTrackingNumber") String planTrackingNumber);


        // 根据 matchID 和多个 documentTypeID 查询文件ID集合
        @Select("<script>" +
                "SELECT documentTypeID, fileID " +
                "FROM cy_file " +
                "WHERE matchID = #{matchID} " +
                "AND documentTypeID IN " +
                "<foreach collection='documentTypeIDs' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
                "AND deleteFlag = 0" +
                "</script>")
        List<Map<String, Object>> findFileIDsByMatchIDAndDocumentTypeIDs(@Param("matchID") String matchID,
                                                                         @Param("documentTypeIDs") List<Integer> documentTypeIDs);



        // 根据 matchID 查询文件ID集合
        @Select("SELECT fileID FROM cy_file WHERE matchID = #{matchID} AND deleteFlag = 0")
        List<String> findFileIDsByMatchID(@Param("matchID") String matchID);

        // 根据 documentTypeID 查询文件ID集合
        @Select("SELECT fileID FROM cy_file WHERE documentTypeID = #{documentTypeID} AND deleteFlag = 0")
        List<String> findFileIDsByDocumentTypeID(@Param("documentTypeID") Integer documentTypeID);


        // 根据 matchID 和 documentTypeID 查询文件ID集合
        @Select("SELECT fileID FROM cy_file WHERE matchID = #{matchID} AND documentTypeID = #{documentTypeID} AND deleteFlag = 0")
        List<String> findFileIDsByMatchIDAndDocumentTypeID(@Param("matchID") String matchID,
                                                           @Param("documentTypeID") Integer documentTypeID);
        // 根据 fileID 查找文件记录
        @Select("SELECT * FROM cy_file WHERE fileID = #{fileID}")
        CYFile findFileById(String fileID);

        @Update("UPDATE cy_file SET updatedBy = #{updatedBy}, updatedAt = #{updatedAt}, fileURL = #{fileURL}, deleteFlag = #{deleteFlag} WHERE fileID = #{fileID}")
        int updateFile(@Param("fileID") String fileID,
                       @Param("updatedBy") String updatedBy,
                       @Param("updatedAt") String updatedAt,
                       @Param("fileURL") String fileURL,
                       @Param("deleteFlag") boolean deleteFlag);

        @Insert("INSERT INTO cy_file(fileID,fileName, folderID, documentTypeID, matchID, documentTypeName, versionNumber, planTrackingNumber, createdBy, createdAt, fileURL) " +
                "VALUES(#{fileID},#{fileName}, #{folderID}, #{documentTypeID}, #{matchID}, #{documentTypeName}, #{versionNumber}, #{planTrackingNumber}, #{createdBy}, NOW(), #{fileURL})")
        @Options(useGeneratedKeys = true, keyProperty = "fileID")
        int insertFile(CYFile file);

        @Select("SELECT * FROM cy_file WHERE documentTypeName = #{documentTypeName} AND matchID = #{matchID} AND deleteFlag = 0")
        CYFile findByFileNameAndMatchID(@Param("documentTypeName") String documentTypeName, @Param("matchID") Integer matchID);

        @Update("UPDATE CY_FILE SET deleteFlag = 1, updatedAt = NOW() WHERE fileID = #{fileID}")
        int markAsDeleted(@Param("fileID") String fileID);

        @Select("SELECT * FROM cy_file WHERE fileID = #{fileId} AND deleteFlag = 0")
        CYFile selectByFileId(@Param("fileId") String fileId);

        /**
         * 批量查询：根据 fileId 列表返回对应的 fileURL
         * 注意这里列名和实体属性都是驼峰：
         *   表列：fileID, fileURL, deleteFlag
         */
        @Select("<script>\n"
                + "SELECT fileURL\n"
                + "  FROM cy_file\n"
                + " WHERE fileID IN\n"
                + "   <foreach collection='fileIds' item='id' open='(' separator=',' close=')'>\n"
                + "     #{id}\n"
                + "   </foreach>\n"
                + "   AND deleteFlag = 0\n"
                + "</script>")
        List<String> selectFileURLsByIds(@Param("fileIds") List<String> fileIds);
}
