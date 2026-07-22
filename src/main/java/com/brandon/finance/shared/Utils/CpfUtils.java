package com.brandon.finance.shared.Utils;

public class CpfUtils {
    
    public static String normalize(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
