package com.sky.controller.admin;

import com.aliyuncs.ecs.model.v20140526.DescribeTaskAttributeResponse;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation(value = "员工登录")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        //封装vo对象，返回前端用数据传递的result数据data是vo对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工: {}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return result对象，泛型是pageresult对象（data是pr对象）
     */
    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询参数为: {}",employeePageQueryDTO);
        PageResult pageResult=employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用账号
     * @param status 当前状态
     * @param id 修改的用户id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用账号")
   public Result StartorStop(@PathVariable Integer status, long id){
        log.info("启用禁用员工账号：{}，{}" ,status,id);
        employeeService.StartorStop(status,id);
        return Result.success();

   }

    /**
     * 修改信息第一步，（回显）查询员工信息，路径参数传id,返回json result
     * @param id
     * @return
     */
   @GetMapping("/{id}")
   @ApiOperation("查询员工信息")
   public Result<Employee> getById(@PathVariable long id){
       Employee employee =employeeService.getById(id);
       return Result.success(employee);
   }

    /**
     * 修改信息第二步，修改员工信息.一般修改put,新增post.不确定的提交请求，无脑post。以接口文档为准！！
     * @param employeeDTO
     * @return
     */
   @PutMapping
   @ApiOperation("编辑员工信息")
   public Result update(@RequestBody EmployeeDTO employeeDTO){
       employeeService.update(employeeDTO);
       return Result.success();
   }

}
