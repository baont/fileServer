package com.examplefoobar.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Objects;

/**
 * FIXME: This class was written by a really junior software engineer.
 * It seems there are a lot of rooms for improvement in this class.
 * Please give us your feedback about this class (with suggestions if possible.)
 */

/**
 * A thread-safe container that stores a group ID and members.
 * <p>
 * This class can store <tt>Member</tt> and/or <tt>AdminMember</tt>.
 * Also, it can start and stop a background task that writes a member list to specified 2 files.
 * <p>
 * This class is called many times, so it should have a good performance.
 * <p>
 * Example usage:
 * <p>
 * DisappointingGroup group = new DisappointingGroup("group-001");
 * <p>
 * group.addMember(new DisappointingGroup.Member("member-100", 42));
 * group.addMember(new DisappointingGroup.AdminMember("admin-999", 30));
 * group.addMember(new DisappointingGroup.Member("member-321", 15));
 * <p>
 * group.startLoggingMemberList10Times("/tmp/output.primary", "/tmp/output.secondary");
 */
public class DisappointingGroup {

    private String groupId;

    private HashSet<Member> members;

    private boolean isRunning;
    private boolean shouldStop;

    static class Member {
        String memberId;
        int age;

        Member(String memberId, int age) {
            this.memberId = memberId;
            this.age = age;
        }

        String getMemberId() {
            return memberId;
        }

        int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Member member = (Member) o;
            return Objects.equals(memberId, member.memberId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(memberId);
        }
    }

    static class AdminMember extends Member {
        AdminMember(String memberId, int age) {
            super(memberId, age);
        }
    }

    public DisappointingGroup(String groupId) {
        this.groupId = groupId;
        this.members = new HashSet<>();
    }

    public void addMember(Member member) {
        members.add(member);
    }

    private String getDecoratedMemberId(Member member) {
        if (member instanceof Member) {
            return member.getMemberId() + "(normal)";
        } else if (member instanceof AdminMember) {
            return member.getMemberId() + "(admin)";
        }
        return null;
    }

    private String getMembersAsStringFlooringAge() {
        String buf = "";
        for (Member member : members) {
            // Floor the age: e.g. 37 -> 30
            Integer flooredAge = (member.getAge() / 10) * 10;
            String decoratedMemberId = getDecoratedMemberId(member);
            buf += String.format("memberId=%s, age=%d¥n", decoratedMemberId, flooredAge);
        }
        return buf;
    }

    /**
     * Run a background task that writes a member list to specified files 10 times in background thread
     * so that it doesn't block the caller's thread.
     * <p>
     * Only one thread is allowed to run at once
     * - When this method is called and another thread is running, the method call should just return w/o starting any thread
     * - When this method is called and another thread is already finished, the method call should start a new thread
     */
    public void startLoggingMemberList10Times(final String outputFilePrimary, final String outputFileSecondary) {
        // Only one thread is allowed to run at once
        if (isRunning) {
            return;
        }
        isRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!shouldStop) {
                    if (i++ >= 10)
                        break;

                    FileWriter writer0 = null;
                    FileWriter writer1 = null;
                    try {
                        String membersStr = DisappointingGroup.this.getMembersAsStringFlooringAge();

                        writer0 = new FileWriter(new File(outputFilePrimary));
                        writer0.append(membersStr);

                        writer1 = new FileWriter(new File(outputFileSecondary));
                        writer1.append(membersStr);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Unexpected error occurred. Please check these file names. outputFilePrimary="
                                        + outputFilePrimary + ", outputFileSecondary=" + outputFileSecondary);
                    } finally {
                        try {
                            if (writer0 != null)
                                writer0.close();

                            if (writer1 != null)
                                writer1.close();
                        } catch (Exception ignored) {
                            // Do nothing since there isn't anything we can do here, right?
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Stop the background task started by <tt>startLoggingMemberList10Times()</tt>
     */
    public void stopPrintingMemberList() {
        shouldStop = true;
    }

    public static void main(String[] args) {
        DisappointingGroup group = new DisappointingGroup("group-001");

        group.addMember(new DisappointingGroup.Member("member-100", 42));
        group.addMember(new DisappointingGroup.Member("member-100", 43));
        group.addMember(new DisappointingGroup.AdminMember("admin-999", 30));
        group.addMember(new DisappointingGroup.Member("member-321", 15));

        group.startLoggingMemberList10Times("C:/Users/nec12/OneDrive/Desktop/output.primary",
                "C:/Users/nec12/OneDrive/Desktop/output.secondary");
    }
}
