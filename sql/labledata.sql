-- 插入测试数据

-- 1. 供应商信息表 (label_hd_supplier)
INSERT INTO label_hd_supplier (
    SupplierID, supplier_name, supplier_num, create_time, supplier_desc,
    create_user_id, create_user_name, update_time, update_user_id,
    update_user_name, del_flag, template_id
) VALUES
      (1, '供应商A', 'SUP-A001', '2024-01-15 10:00:00', '主要供应阀门材料',
       1001, '张三', '2024-04-10 15:30:00', 1002, '李四', 0, 5001),
      (2, '供应商B', 'SUP-B002', '2024-02-20 11:20:00', '提供高质量钢材',
       1003, '王五', '2024-05-12 09:45:00', 1004, '赵六', 0, 5002);

-- 2. 原材料报告表 (MaterialReport)
INSERT INTO label_MaterialReport (
    ReportID, SupplierID, HeatNumber, MaterialCode, DownloadLink,
    BrowseLinks, creat_time
) VALUES
      (10001, 1, 'HN-1001', 'MAT-2001', 'http://example.com/download/hm-1001.pdf',
       'http://example.com/browse/hm-1001', '2024-03-01 08:00:00'),
      (10002, 2, 'HN-1002', 'MAT-2002', 'http://example.com/download/hm-1002.pdf',
       'http://example.com/browse/hm-1002', '2024-04-05 14:30:00');

-- 3. 合同信息表 (Contract)
INSERT INTO label_Contract (
    ContractCode, ContractDate, ContractName, CustomerName
) VALUES
      ('CON-3001', '2024-03-15', '阀门采购合同', '客户X'),
      ('CON-3002', '2024-04-20', '钢材供应合同', '客户Y');

-- 4. 产品信息表 (label_hd_product)
INSERT INTO label_hd_product (
    ID, product_type, ContractID, product_num, product_category, design_no,
    gctj, gcyl, ftcz, zxjg, zyxs, qyyl, gzwd, zzrq, xc,
    create_time, create_user_id, create_user_name, update_time,
    update_user_name, update_user_id, del_flag, request_id, fxcz,
    fzcz, lltx, cv, dy, factory_name, mplx
) VALUES
      (5001, '型号A-1000', 1, 'PROD-4001', '球阀', 'D-001', '50mm', '10bar',
       '碳钢', '电动', '开关', '6bar/220V', '150°C', '2024-05-01', '行程A',
       '2024-03-20 09:00:00', 1001, '张三', '2024-06-10 16:00:00',
       '李四', 1002, 0, 'REQ-9001', 'SS304', 'SS316', '线性', '1.2', 'DY-1',
       '工厂A', '类型1'),
      (5002, '型号B-2000', 2, 'PROD-4002', '闸阀', 'D-002', '75mm', '15bar',
       '不锈钢', '气动', '调节', '8bar/110V', '200°C', '2024-06-15', '行程B',
       '2024-04-25 10:30:00', 1003, '王五', '2024-07-20 11:15:00',
       '赵六', 1004, 0, 'REQ-9002', 'SS304', 'SS316', '非线性', '1.5', 'DY-2',
       '工厂B', '类型2');

-- 5. 用料清单表 (ProductParameter)
INSERT INTO label_ProductParameter (
    ID, ProductID, SelModelId, ERPPlanOrderId, Material_Code,
    Material_Name, Specification, HeatNumber
) VALUES
      (6001, 5001, 7001, 8001, 'MAT-2001', '碳钢', '规格A', 'HN-1001'),
      (6002, 5002, 7002, 8002, 'MAT-2002', '不锈钢', '规格B', 'HN-1002');

-- 6. 合格证表 (Certificate)
INSERT INTO label_Certificate (
    ProductID, IssueDate, DownloadLink, BrowseLinks
) VALUES
      ('PROD-4001', '2024-05-10', 'http://example.com/certificate/prod-4001.pdf',
       'http://example.com/certificate/browse/prod-4001'),
      ('PROD-4002', '2024-06-20', 'http://example.com/certificate/prod-4002.pdf',
       'http://example.com/certificate/browse/prod-4002');

-- 7. 产品检验报告表 (InspectionReport)
INSERT INTO label_InspectionReport (
    ProductID, InspectionDetails, InspectionDate, DownloadLink, BrowseLinks
) VALUES
      ('PROD-4001', '检验通过，符合所有标准。', '2024-05-15',
       'http://example.com/inspection/prod-4001.pdf',
       'http://example.com/inspection/browse/prod-4001'),
      ('PROD-4002', '检验通过，符合所有标准。', '2024-06-25',
       'http://example.com/inspection/prod-4002.pdf',
       'http://example.com/inspection/browse/prod-4002');

-- 8. 材质理化报告表 (PhysicalChemicalReport)
INSERT INTO label_PhysicalChemicalReport (
    HeatNumber, ReportDetails, ReportDate, DownloadLink, BrowseLinks
) VALUES
      ('HN-1001', '理化性质符合标准。', '2024-03-05',
       'http://example.com/physchem/hm-1001.pdf',
       'http://example.com/physchem/browse/hm-1001'),
      ('HN-1002', '理化性质符合标准。', '2024-04-10',
       'http://example.com/physchem/hm-1002.pdf',
       'http://example.com/physchem/browse/hm-1002');

-- 9. 现场装箱照片管理表 (PackingPhoto)
INSERT INTO label_PackingPhoto (
    ProductID, PhotoPath, UploadDate, DownloadLink, BrowseLinks
) VALUES
      (5001, '/images/packing/prod-4001-1.jpg', '2024-05-15',
       'http://example.com/packing/prod-4001-1.jpg',
       'http://example.com/packing/browse/prod-4001-1'),
      (5002, '/images/packing/prod-4002-1.jpg', '2024-06-25',
       'http://example.com/packing/prod-4002-1.jpg',
       'http://example.com/packing/browse/prod-4002-1');

-- 10. 说明书表 (Manual)
INSERT INTO label_Manual (
    product_type, ManualPath, UploadDate, DownloadLink, BrowseLinks
) VALUES
      ('型号A-1000', '/manuals/prod-4001-manual.pdf', '2024-05-20',
       'http://example.com/manuals/prod-4001-manual.pdf',
       'http://example.com/manuals/browse/prod-4001-manual.pdf'),
      ('型号B-2000', '/manuals/prod-4002-manual.pdf', '2024-06-30',
       'http://example.com/manuals/prod-4002-manual.pdf',
       'http://example.com/manuals/browse/prod-4002-manual.pdf');

-- 11. 交工资料表 (DeliveryDoc)
INSERT INTO label_DeliveryDoc (
    ProductID, DeliveryPath, DeliveryDate, DownloadLink, BrowseLinks
) VALUES
      (5001, '/deliveries/prod-4001-delivery.pdf', '2024-05-25',
       'http://example.com/deliveries/prod-4001-delivery.pdf',
       'http://example.com/deliveries/browse/prod-4001-delivery.pdf'),
      (5002, '/deliveries/prod-4002-delivery.pdf', '2024-07-05',
       'http://example.com/deliveries/prod-4002-delivery.pdf',
       'http://example.com/deliveries/browse/prod-4002-delivery.pdf');
