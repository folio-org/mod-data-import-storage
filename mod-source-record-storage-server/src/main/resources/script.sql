do $$ declare
  rec record;
  recordIndex integer := 0;
  logFrequency integer := 10000;
begin
  RAISE INFO 'Start time: %', NOW();
  for rec in (select id, content from fs09000000_mod_source_record_storage.marc_records_lb x)
            loop

              IF (recordIndex % logFrequency = 0) THEN
      				RAISE INFO 'RecordIndex: %', recordIndex;
    			END IF;
              recordIndex := recordIndex + 1 ;

              insert into fs09000000_mod_source_record_storage.marc_indexers (field_no, ind1, ind2, subfield_no, value, marc_id)
                    (with vals as (select value
                                   from jsonb_array_elements((
                                       select value
                                       from jsonb_each(rec.content) x
                                       where key = 'fields')) y),
                          fields as (select x.key as field_no, x.value as field_value
                                     from vals,
                                          jsonb_each(vals.value) x),
                          fields_subfields as (
                              select field_no,
                                     trim(field_value ->> 'ind1'::text) ind1,
                                     trim(field_value ->> 'ind2')       ind2,
                                     field_value -> 'subfields'         subfields,
                                     field_value    from fields),
                          marc_raw as (
                              select fs.field_no, fs.ind1, fs.ind2, fs.field_value, null::text subfield_no, null::text subfield_value
                              from fields_subfields fs
                              where subfields is null
                              union all
                              select fs.field_no, fs.ind1, fs.ind2, fs.field_value, subfs.key::text subfield_no, subfs.value::text subfield_value
                              from fields_subfields fs,
                                   jsonb_array_elements(fs.subfields) sx,
                                   jsonb_each(sx.value) subfs
                              where subfields is not null),
                          marc as (
                              select m.field_no,
                                     CASE WHEN ind1 IS NULL or ind1 = '' THEN '#' ELSE ind1 END
                                        as ind1,
                                     CASE WHEN ind2 IS NULL or ind2 = '' THEN '#' ELSE ind2 END
                                        as ind2,
                                     CASE WHEN subfield_no IS NULL or trim(subfield_no) = '' THEN '0' ELSE subfield_no END as subfield_no,
                                     trim(both '"' from coalesce(subfield_value, field_value::text))
                                        as value
                              from marc_raw m)
                      select distinct lower(field_no) field_no, ind1, ind2, subfield_no, value, rec.id marc_id from marc);
                          --
                            insert into fs09000000_mod_source_record_storage.marc_indexers_leader(p_00_04, p_05, p_06, p_07, p_08, p_09, p_10, p_11, p_12_16, p_17, p_18, p_19, p_20, p_21, p_22, marc_id)
                              (select     substring(value from 1 for 5)   p_00_04,
                                          substring(value from 6 for 1)   p_05,
                                          substring(value from 7 for 1)   p_06,
                                          substring(value from 8 for 1)   p_07,
                                          substring(value from 9 for 1)   p_08,
                                          substring(value from 10 for 1)  p_09,
                                          substring(value from 11 for 1)  p_10,
                                          substring(value from 12 for 1)  p_11,
                                          substring(value from 13 for 5)  p_12_16,
                                          substring(value from 18 for 1)  p_17,
                                          substring(value from 19 for 1)  p_18,
                                          substring(value from 20 for 1)  p_19,
                                          substring(value from 21 for 1)  p_20,
                                          substring(value from 22 for 1)  p_21,
                                          substring(value from 23 for 1)  p_22,
                                          marc_id
                                from (select replace(lower(trim(both '"' from value::text)), ' ', '#') as value,
                                         rec.id    marc_id
                                         from jsonb_each(rec.content) x
                                         where key = 'leader') y);
                  end loop;
 RAISE INFO 'End time: %', NOW();
 end;
$$;
