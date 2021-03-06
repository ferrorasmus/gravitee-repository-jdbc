/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.jdbc.management;

import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.jdbc.orm.JdbcColumn;
import io.gravitee.repository.jdbc.orm.JdbcObjectMapper;
import io.gravitee.repository.management.api.PageRevisionRepository;
import io.gravitee.repository.management.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Date;
import java.util.*;

import static io.gravitee.repository.jdbc.common.AbstractJdbcRepositoryConfiguration.escapeReservedWord;
import static io.gravitee.repository.jdbc.orm.JdbcColumn.getDBName;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcPageRevisionRepository implements PageRevisionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPageRevisionRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final JdbcObjectMapper ORM = JdbcObjectMapper.builder(PageRevision.class, "page_revisions", "page_id")
            .addColumn("page_id", Types.NVARCHAR, String.class)
            .addColumn("revision", Types.INTEGER, int.class)
            .addColumn("name", Types.NVARCHAR, String.class)
            .addColumn("content", Types.NVARCHAR, String.class)
            .addColumn("hash", Types.NVARCHAR, String.class)
            .addColumn("contributor", Types.NVARCHAR, String.class)
            .addColumn("created_at", Types.TIMESTAMP, Date.class)
            .build();

    @Override
    public Optional<PageRevision> findById(String pageId, int revision) throws TechnicalException {
        LOGGER.debug("JdbcPageRevisionRepository.findById({}, {})",pageId, revision);
        try {
            final List<PageRevision> items = jdbcTemplate.query("select * from page_revisions where page_id = ? and revision = ?"
                    , ORM.getRowMapper()
                    , pageId
                    , revision
            );
            return items.stream().findFirst();
        } catch (final Exception ex) {
            LOGGER.error("Failed to find page revision by id", ex);
            throw new TechnicalException("Failed to find page revision by id", ex);
        }
    }

    @Override
    public PageRevision create(PageRevision item) throws TechnicalException {
        LOGGER.debug("JdbcPageRevisionRepository.create({})", item);
        try {
            jdbcTemplate.update(ORM.buildInsertPreparedStatementCreator(item));
            return findById(item.getPageId(), item.getRevision()).orElse(null);
        } catch (final Exception ex) {
            LOGGER.error("Failed to create page revision", ex);
            throw new TechnicalException("Failed to create page revision", ex);
        }
    }

    @Override
    public void deleteAllByPageId(String pageId) throws TechnicalException {
        LOGGER.debug("JdbcPageRepository.deleteAllByPageId({})", pageId);
        try {
            jdbcTemplate.update("delete from page_revisions p where p.page_id = ?", pageId);
        } catch (final Exception ex) {
            LOGGER.error("Failed to delete revisions fo page : {}", pageId, ex);
            throw new TechnicalException("Failed to delete page revisions", ex);
        }
    }

    @Override
    public List<PageRevision> findAllByPageId(String pageId) throws TechnicalException {
        LOGGER.debug("JdbcPageRepository.findAllByPageId({})", pageId);
        try {
            List<PageRevision> result = jdbcTemplate.query("select p.* from page_revisions p where p.page_id = ? order by revision desc"
                    , ORM.getRowMapper()
                    , pageId);
            LOGGER.debug("JdbcPageRepository.findLastByPageId({}) = {}", pageId, result);
            return result;
        } catch (final Exception ex) {
            LOGGER.error("Failed to find revisions by page id: {}", pageId , ex);
            throw new TechnicalException("Failed to find revisions by page id", ex);
        }
    }

    @Override
    public Optional<PageRevision> findLastByPageId(String pageId) throws TechnicalException {
        LOGGER.debug("JdbcPageRepository.findLastByPageId({})", pageId);
        try {
            List<PageRevision> rows = jdbcTemplate.query("select p.* from page_revisions p where p.page_id = ? order by revision desc limit 1"
                    , ORM.getRowMapper()
                    , pageId);
            Optional<PageRevision> result = rows.stream().findFirst();
            LOGGER.debug("JdbcPageRepository.findLastByPageId({}) = {}", pageId, result);
            return result;
        } catch (final Exception ex) {
            LOGGER.error("Failed to find last revision by page id: {}", pageId , ex);
            throw new TechnicalException("Failed to find last revision by page id", ex);
        }
    }
}