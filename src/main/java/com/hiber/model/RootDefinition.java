package com.hiber.model;

import java.util.List;

public class RootDefinition {

    private Meta meta;
    private List<FieldDefinition> seq;

    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    public List<FieldDefinition> getSeq() { return seq; }
    public void setSeq(List<FieldDefinition> seq) { this.seq = seq; }
}