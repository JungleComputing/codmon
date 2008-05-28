#!/usr/bin/perl

# Outcmp specialized for GMI.
# Usage : perl outcmp_mpj_wrapper.pl <directory> <nbnodes> <args>
#
# See outcmp_wrapper.pl for details

my ($localdir, $nbnodes, @args) = @ARGV;
my $runargs = join " ", @args;

chdir($localdir);

$runargs = `echo $runargs`;    # To interpret `cat run-args`
my $class = $runargs;
chomp($class);

$class =~ s/^(.*?\s)*?([0-9a-zA-Z_.]*)(\s.*$|$)/$2/;

#print $class."\n";
#exit;

# Test if we create a new file

my $dir = ".";
if (-d ".codmon-samples") {
    $dir = ".codmon-samples";
}

my $new = 0;
if (!(-e "$dir/$class.out.sample")) {
    $new = 1;
}

my $cmd = "perl /home1/codmon/codmon/wrappers/outcmp_wrapper.pl $localdir $dir/$class.out.sample /home1/codmon/codmon/wrappers/run-mpj  $nbnodes $runargs"; 
#print $cmd."\n";
`$cmd`;

if ($new == 1) {    # Add to cvs
    `svn add $dir/$class.out.sample`;
    `svn commit -m "Create" $dir/$class.out.sample`;
    print "added to SVN\n";
}

exit($?>>8);
