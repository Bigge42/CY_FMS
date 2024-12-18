-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹', '3', '1', 'cyfolder', 'fms/cyfolder/index', 1, 0, 'C', '0', '0', 'fms:cyfolder:list', '#', 'admin', sysdate(), '', null, '文件夹菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'fms:cyfolder:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'fms:cyfolder:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'fms:cyfolder:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'fms:cyfolder:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件夹导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'fms:cyfolder:export',       '#', 'admin', sysdate(), '', null, '');