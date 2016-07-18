package com.ksss.splintter.features.group.domain;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * // TODO: 7/17/16 Add auto-value!!
 * Created by Nahuel Barrios on 7/17/16.
 */
public class Group {
    private String name;
    private List<Member> members;

    public Group(@NonNull String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    @NonNull
    public List<Member> getMembers() {
        return members;
    }

    public void addMember(Member member) {
        members.add(member);
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", members=" + members +
                '}';
    }
}
