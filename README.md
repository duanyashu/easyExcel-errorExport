# easyExcel的封装，实现导入错误信息的批注提示导出
### 使用说明
    @RequestMapping("/file")
        @ResponseBody
        public ReslutData uploadFile(HttpServletResponse response,MultipartFile file){
            try {
                // 参数1 response 错误导出使用 参数2 导入文件  参数3 导入对应的实体对象
                EasyExcelUtils.webImportExcel(response,file.getInputStream(), UserImpost.class);
                //第四个参数是否导出错误数据 默认为true 设置false 可以自己处理错误数据
            //EasyExcelListener easyExcelListener = EasyExcelUtils.webImportExcel(response,file.getInputStream(), UserImpost.class,false);
                // 获取错误数据结果集
                List<ExcelImportErrDto> errorList = easyExcelListener.getErrList();
                //获取正确数据结果集
                List<UserImpost> successList = easyExcelLIstener.getSuccessList();
               // 第三个参数 ExcelCheckManager实现自主校验 正常在service接口继承 实现自主校验和存入数据库
                EasyExcelUtils.webImportExcel(response,file.getInputStream(),customCheckService, UserImpost.class);
                return ReslutData.success();
            } catch (Exception e) {
                return  ReslutData.fail("导入失败：错误"+e.getMessage());
            }
        }
    
#### 自主校验
- service接口继承ExcelCheckManager
    
        public interface UserService extends ExcelCheckManager<UserImpost> {
        }
- service接口继承ExcelCheckManager   

        public class UserServiceImpl implements UserService {    
            //不合法名字
            public static final String ERR_NAME = "sb";
            @Override
            public ExcelCheckResult checkImportExcel(List<UserImpost> userList) {
                //成功结果集
                List<UserImpost> successList = new ArrayList<>();
                //错误数组
                List<ExcelImportErrObjectDto> errList = new ArrayList<>();
                Map<Integer,String> cellErrColMap= new HashMap<>();
                for (int i = 0;i<userList.size() ;i++) {
                    UserImpost user = userList.get(i);
                    //错误信息
                    //根据自己的业务去做判断
                    if (ERR_NAME.equals(user.getName())){
                        try {
                        //获取当前字段对应的索引
                            int index = UserImpost.class.getDeclaredField("name").getAnnotation(ExcelProperty.class).index();
                            cellErrColMap.put(index,"请输入正确的名字");
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                    if (cellErrColMap.size()==0){
                        //没有错误信息 保存到成功列表
                        successList.add(user);
                    }else{
                        //保存到错误列表
                        errList.add(new ExcelImportErrObjectDto(user,cellErrColMap));
                    }
                }
                //在这里将校验成功的数据批量写入数据库
                System.out.println(successList.toString());
                return new ExcelCheckResult(errList);
            }
        }
### 实体类

    public class UserImpost {
    
        //名称
        @ExcelProperty(index = 0,value = "名称")
        @ColumnWidth(30)
        @Length(max = 10)
        private String name;
    
        //性别
        @ExcelProperty(index = 1,value = "性别")
        @ColumnWidth(30)
        @Length(max = 2)
        @NotBlank(message = "性别不能为空")
        //正则校验  校验格式统一在ExcelPatternMsg类中
        @Pattern(regexp = ExcelPatternMsg.SEX,message = ExcelPatternMsg.SEX_MSG)
        private String sex;
    
        //年龄
        @ExcelProperty(index = 2,value = "年龄")
        @ColumnWidth(30)
        @Pattern(regexp = ExcelPatternMsg.NUMBER,message = ExcelPatternMsg.NUMBER_MSG)
        private String age;
      
        //生日
        @ExcelProperty(index = 3,value = "生日")
        @Pattern(regexp = ExcelPatternMsg.DATE2,message = ExcelPatternMsg.DATE2_MSG)
        private String birthday;
    
    }
   