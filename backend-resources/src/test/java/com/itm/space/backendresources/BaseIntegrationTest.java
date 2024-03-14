package com.itm.space.backendresources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    private final ObjectWriter contentWriter = new ObjectMapper()
            .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
            .writer()
            .withDefaultPrettyPrinter();

    @Autowired
    protected MockMvc mvc;

    //////////////////////////////////////////////////////////////////////
    @Autowired
    private WebApplicationContext wac;

    @Before
    private void startUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    ///////////////////////////////////////////////////////////////////////

    protected MockHttpServletRequestBuilder requestToJson(
            MockHttpServletRequestBuilder requestBuilder) {
        return requestBuilder
                .contentType(APPLICATION_JSON);
    }

    protected MockHttpServletRequestBuilder requestWithContent(
            MockHttpServletRequestBuilder requestBuilder,
            Object content) throws JsonProcessingException {
        return requestToJson(requestBuilder)
                .content(contentWriter.writeValueAsString(content));
    }
}
