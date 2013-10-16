#!/usr/bin/perl

# Usage : perl time_wrapper <setdirectory> <cmd>
# Simple wrapper script for build targets.
#   Prints the <test.../> node on STDOUT, the stderr on STDERR, 
#   and propagates the exit value
#
use strict;

my ($dir, @args) = @ARGV;
my $cmd = join " ", @args;

$cmd =~ s/&lt;/</g;
$cmd =~ s/&gt;/>/g;

#print $dir;
chdir($dir);

my $start = time();

#print $cmd;
my $err = `$cmd 2>&1 1>/dev/null`;

my $end = time();
my $length = $end-$start;

print "<test id=\"time\" name=\"Time\" value=\"$length\" unit=\"s\"/>\n";
#check op errors
if ($?>>8 != 0) {
    $err =~ s/\n/<br\/>\n/g;
    print STDERR $err;
}
exit($?>>8);

