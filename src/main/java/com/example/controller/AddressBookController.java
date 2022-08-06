package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.domain.AddressBook;
import com.example.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址管理
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookServicel;

    /**
     * 查询显示地址信息
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> getList(){
        LambdaQueryWrapper<AddressBook> lqw=new LambdaQueryWrapper<>();
        log.info("当前用户ID为："+BaseContext.getCurrentId());
        lqw.eq(BaseContext.getCurrentId()!=null,AddressBook::getUserId,BaseContext.getCurrentId());
        List<AddressBook> list = addressBookServicel.list(lqw);
        return R.success(list);
    }

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> saveAddress(@RequestBody AddressBook addressBook){
        log.info("地址信息："+addressBook);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookServicel.save(addressBook);
        return R.success("添加成功");
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> getDefault(@RequestBody AddressBook addressBook){
        //先把所有的default归为非默认
        Long id = addressBook.getId();
        LambdaUpdateWrapper<AddressBook> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BaseContext.getCurrentId()!=null,AddressBook::getUserId,BaseContext.getCurrentId());
        lambdaUpdateWrapper.set(AddressBook::getIsDefault,0);
        addressBookServicel.update(lambdaUpdateWrapper);
        //再把指定ID的地址改为默认
        addressBook.setIsDefault(1);
        addressBookServicel.updateById(addressBook);
        return R.success("设置完成");
    }
    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        log.info("查询默认地址");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook =  addressBookServicel.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }
    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookServicel.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    @DeleteMapping
    public R<String> deleteAddress(Long ids){
        log.info(ids.toString());
        LambdaQueryWrapper<AddressBook> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ids!=null,AddressBook::getId,ids);
        addressBookServicel.remove(lqw);
        return R.success("删除成功");
    }


}
