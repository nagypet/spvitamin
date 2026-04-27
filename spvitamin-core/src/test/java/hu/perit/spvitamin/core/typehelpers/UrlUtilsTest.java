/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.core.typehelpers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilsTest
{
    @Test
    void extractDomain_shouldStripWwwPrefix()
    {
        assertThat(UrlUtils.extractDomain("https://www.example.com/path/page")).isEqualTo("https://example.com");
    }


    @Test
    void extractDomain_shouldNotStripNonWwwSubdomain()
    {
        assertThat(UrlUtils.extractDomain("https://sub.example.com/path")).isEqualTo("https://sub.example.com");
    }


    @Test
    void extractDomain_shouldStripPath()
    {
        assertThat(UrlUtils.extractDomain("https://example.com/some/deep/path")).isEqualTo("https://example.com");
    }


    @Test
    void extractDomain_shouldStripQueryAndFragment()
    {
        assertThat(UrlUtils.extractDomain("https://example.com/page?q=1&x=2#section")).isEqualTo("https://example.com");
    }


    @Test
    void extractDomain_shouldPreserveScheme()
    {
        assertThat(UrlUtils.extractDomain("http://www.example.com/path")).isEqualTo("http://example.com");
    }


    @Test
    void extractDomain_shouldDropPort()
    {
        assertThat(UrlUtils.extractDomain("https://example.com:8080/path")).isEqualTo("https://example.com");
    }


    @Test
    void extractDomain_shouldReturnNull_whenInvalidUrl()
    {
        assertThat(UrlUtils.extractDomain("not a valid url")).isNull();
        assertThat(UrlUtils.extractDomain("*")).isNull();
        assertThat(UrlUtils.extractDomain("invalid-url")).isNull();
    }


    @Test
    void extractDomain_shouldReturnNull_whenNoHost()
    {
        assertThat(UrlUtils.extractDomain("")).isNull();
    }


    @Test
    void extractDomain_realWorld_szamlazzHu()
    {
        assertThat(UrlUtils.extractDomain("https://www.szamlazz.hu/szamla/fiok/jcew3vf7vx5t?szfejguid=abc")).isEqualTo("https://szamlazz.hu");
        assertThat(UrlUtils.extractDomain("https://szamlazz.hu/szamla/?action=szamlapdf&szfej_id=840054342")).isEqualTo("https://szamlazz.hu");
    }
}
