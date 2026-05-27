package com.freightos.admin.application.code.country;

import com.freightos.admin.application.code.country.command.CreateCountryCommand;
import com.freightos.admin.domain.code.country.entity.Country;
import org.springframework.stereotype.Component;

@Component
public class CountryFactory {

    public Country from(CreateCountryCommand command) {
        return Country.create(command.countryCode(), command.name(), command.nameEn(), command.active());
    }
}
