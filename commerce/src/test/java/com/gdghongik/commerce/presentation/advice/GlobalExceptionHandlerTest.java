package com.gdghongik.commerce.presentation.advice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    @Test
    void 낙관적_락_충돌은_409로_응답한다() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ConflictController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
    }

    @RestController
    static class ConflictController {
        @GetMapping("/test/optimistic-lock")
        void conflict() {
            throw new ObjectOptimisticLockingFailureException("Product", 1L);
        }
    }
}
