package com.bao.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilterUtil {

    PrefixTreeNode root = new PrefixTreeNode();

    /**
     * 初始化前缀树
     */
    @PostConstruct
    private void init() {
        // 读取敏感词文件
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String keyword;
            while ((keyword = br.readLine()) != null) {
                addKeyword(keyword);// 添加到前缀树上
            }
        } catch (IOException e) {
        }
    }

    public boolean isLawful(String keyword) {
        if (StringUtils.isBlank(keyword)) return false;

        PrefixTreeNode positionNode = root;
        int begin = 0;
        int position = 0;

        char[] cs = keyword.toCharArray();
        while (begin < keyword.length()) {
            if (position < keyword.length()) {
                // 如果是特殊符号  **赌**博**
                if (isSymbol(cs[position])) {
                    if (positionNode == root) {// 指向根节点，说明还没出现敏感词
                        begin = ++position;
                    }else {
                        // 跳过这个敏感词
                        position++;
                    }
                    continue;
                }

                PrefixTreeNode subNode = positionNode.getSubNode(cs[position]);
                if (subNode == null) {
                    // 更新根节点指向
                    positionNode = root;
                    begin = ++position;
                } else if (subNode.isKeywordEnd) {
                    return false;
                } else {// 如果字符出现在前缀树中
                    positionNode = subNode;// 节点指针指向子节点
                    position++;
                }
            }else{// 越界了
                position = ++begin;
                positionNode = root;
            }
        }
        return true;
    }

    /**
     * 判断字符是否为特殊符号
     *
     * @param c
     * @return
     */
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private void addKeyword(String keyword) {
        PrefixTreeNode node = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            PrefixTreeNode subNode = node.getSubNode(c);
            if (subNode == null) {
                subNode = new PrefixTreeNode();
                node.addSubNode(c, subNode);// 新增子节点
            }
            node = subNode;// 指向子节点
            if (i == keyword.length() - 1) {// 如果是前缀树结尾的话
                node.setKeywordEnd(true);
            }
        }
    }

    private class PrefixTreeNode {
        boolean isKeywordEnd = false;

        Map<Character, PrefixTreeNode> subNode = new HashMap<>();

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, PrefixTreeNode node) {
            subNode.put(c, node);
        }

        public PrefixTreeNode getSubNode(Character c) {
            return subNode.get(c);
        }
    }
}
