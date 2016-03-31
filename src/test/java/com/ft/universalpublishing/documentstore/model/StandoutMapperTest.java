package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Standout;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class StandoutMapperTest {

    final StandoutMapper standoutMapper = new StandoutMapper();

    @Test
    public void testMapAttributes() throws Exception {
        final Standout source = new Standout(true, true, false);

        final com.ft.universalpublishing.documentstore.model.read.Standout target = standoutMapper.map(source);

        assertThat(target.isEditorsChoice(), equalTo(source.isEditorsChoice()));
        assertThat(target.isScoop(), equalTo(source.isScoop()));
        assertThat(target.isExclusive(), equalTo(source.isExclusive()));
    }

    @Test
    public void testMapNullStandout() throws Exception {
        final com.ft.universalpublishing.documentstore.model.read.Standout target = standoutMapper.map(null);

        assertThat(target, nullValue());
    }
}
