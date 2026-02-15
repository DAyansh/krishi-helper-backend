package com.ayansh.Backend.Repository;


import com.ayansh.Backend.Model.AppLang;
import com.ayansh.Backend.Model.Language;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepositoryImplementation<Language, Long> {
    Optional<Language> findByLangName(AppLang langName);
}
