package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.CodeRunRequest;
import com.infosys.knowledgegap.dto.CodeRunResponse;

public interface CodeExecutionService {
    CodeRunResponse execute(CodeRunRequest request);
}
