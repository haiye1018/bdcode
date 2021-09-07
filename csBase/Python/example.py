#!/usr/bin/python
# -*- coding: UTF-8 -*-

# 第一个注释
print ("Hello, Python!") # 第二个注释
'''
第三注释
第四注释
'''
 
"""
第五注释
第六注释
"""
'''
python最具特色的就是使用缩进来表示代码块，不需要使用大括号 {}
缩进的空格数是可变的，但是同一个代码块的语句必须包含相同的缩进空格数
'''
# 缩进不一致，会导致运行错误:IndentationError: unindent does not match any outer indentation level
if True:
    print ("Answer")
    print ("True")
else:
    print ("Answer")
    print ("False")

# 多行语句
item_one = 1
item_two = 2
item_three = 3
total = item_one + \
        item_two + \
        item_three
print(total)
# 在 [], {}, 或 () 中的多行语句，不需要使用反斜杠 \
total = ['item_one', 'item_two', 'item_three',
        'item_four', 'item_five']
print(total)

#字符串
str1='123456789'
print(str1)                 # 输出字符串
print(str1[0:-1])           # 输出第一个到倒数第二个的所有字符
print(str1[0])              # 输出字符串第一个字符
print(str1[2:5])            # 输出从第三个开始到第五个的字符
print(str1[2:])             # 输出从第三个开始后的所有字符
print(str1[1:5:2])          # 输出从第二个开始到第五个且每隔一个的字符（步长为2）
print(str1 * 2)             # 输出字符串两次
print(str1 + '你好')        # 连接字符串

#列表
list = ['Google', 'Runoob', 1997, 2000]
print( list[0] )
print(len(list[0:3]))
print([len(x) for x in list if type(x) is str])

#元组与列表类似，不同之处在于元组的元素不能修改
tup1 = ('Google', 'Runoob', 1997, 2000)
print("tup1[0]: ", tup1[0])

#字典
dict = {'Name': 'Runoob', 'Age': 7, 'Class': 'First'}
print("dict['Name']: ", dict['Name'])
print("dict['Age']: ", dict['Age'])

# Python 斐波那契数列实现
def FibonacciSequence(nterms):
    # 第一和第二项
    n1 = 0
    n2 = 1
    count = 2
    # 判断输入的值是否合法
    if nterms <= 0:
       print("请输入一个正整数。")
    elif nterms == 1:
       print("斐波那契数列：")
       print(n1)
    else:
       print("斐波那契数列：")
       print(n1,",",n2,end=" , ")
       while count < nterms:
           nth = n1 + n2
           print(nth,end=" , ")
           # 更新值
           n1 = n2
           n2 = nth
           count += 1

FibonacciSequence(10)