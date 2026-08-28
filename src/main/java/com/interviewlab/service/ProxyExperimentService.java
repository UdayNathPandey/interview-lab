package com.interviewlab.service;

import com.interviewlab.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProxyExperimentService {


    private final Utility utility;

    public void outerMethod()
    {

        utility.printTransactionInfo("Outer");
        innerMethod();
    }

    @Transactional
    public void innerMethod()
    {
        utility.printTransactionInfo("Inner");
    }
}
