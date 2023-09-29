package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
       //对象属性拷贝,前来源后目标。employee属性更多，需要设置。
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置状态,用常量类代表字段，防止硬编码。
        employee.setStatus(StatusConstant.ENABLE);
        //密码,注意默认是123456，但需要加密，也用常量
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //时间
        employee.setCreateTime(LocalDateTime. now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人和修改人的id（当前登录用户的id）,拦截器存进去的，service取出来
        //TODO 后期改为哦当前登录用户的id
       /* employee.setCreateUser(10L);
        employee.setUpdateUser(10L);*/
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询,pageHelper动态拼接limit（每页展示记录数）
     * @param employeePageQueryDTO
     * total为查询到的总记录数，records为当前页记录集合。封装成pageresult对象返回controller
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);
        long total=page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 启用禁用账号
     * @param status
     * @param id
     */
    @Override
    public void StartorStop(Integer status, long id) {
        Employee employee=Employee.builder().status(status).id(id).build();
        employeeMapper.update(employee);
    }

    /**
     * 查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("******");
        return employee;
    }

    /**
     * 修改员工信息，补全字段并调用动态sql。
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();

        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

}
