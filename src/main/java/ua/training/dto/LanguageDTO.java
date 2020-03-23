package ua.training.dto;

import lombok.Getter;
import ua.training.controller.SupportedLanguages;

@Getter
public class LanguageDTO {

    private String name;
    private String choice;

    public void setChoice(String language) {
        this.choice = language;
        this.name = findNameByCode(language);
    }


    public String findNameByCode(String code) {
        String codeLC = code.toLowerCase();

        for (SupportedLanguages lang : SupportedLanguages.values()) {
            if (lang.getCode().equals(codeLC)) {
                return lang.getName();
            }
        }

        return "";
    }

    public SupportedLanguages[] getSupportedLanguages() {
        return SupportedLanguages.values();
    }

}
